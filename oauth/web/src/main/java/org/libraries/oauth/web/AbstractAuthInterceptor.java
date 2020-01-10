package org.libraries.oauth.web;

import org.evan.libraries.utils.Excludor;
import org.evan.libraries.utils.PathUtil;
import org.evan.libraries.utils.StringUtil;
import org.libraries.oauth.model.LoginAccount;
import org.libraries.oauth.model.LoginAccountSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 鉴权控制拦截器,用途 :<br>
 * 1、判断token <br>
 * 2、判断是否登录<br>
 * 3、判断权限<br>
 * <p>
 * create at 2016年5月2日 下午9:13:25
 *
 * @author shen.wei
 * @version %I%, %G%
 */
public abstract class AbstractAuthInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthInterceptor.class);

    /**
     * 没有配置权限控制注解的Methed缓存
     */
    private Set<Method> noAuthCaches = Collections.synchronizedSet(new HashSet<Method>());

    private Map<Method, Set<String>> caches = new ConcurrentHashMap<>();

    private Excludor excludor;
    private UrlPathHelper urlPathHelper;
    private String defaultToken;
    private String defaultTokenSecret;
    private AbstractLoginAccountSession LoginAccountSession;

    public void init() {
        if (excludor == null) {
            excludor = new Excludor();
        }

        if (urlPathHelper == null) {
            urlPathHelper = new UrlPathHelper();
        }

        LOGGER.info(">>>> AuthInterceptor Inited, AuthInterceptor class[{}], {}", this.getClass(), excludor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = urlPathHelper.getPathWithinApplication(request);
        if (PathUtil.matches(requestPath, excludor.getExcludes())) {
            return true;
        }


        if (!HandlerMethod.class.isInstance(handler)) { //只要不是HandlerMethod，都认为是404
            //LOGGER.warn(">>>> URI[{}],{} is not execute instance of HandlerMethod,", requestPath, handler);
            //return false;
            HttpHeaders httpHeaders = new HttpHeaders();
            throw new NoHandlerFoundException(request.getMethod(), requestPath, httpHeaders);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        validate(request, method);

        return true;
    }

    /**
     * 默认验证逻辑，子类可重写
     * @param request
     * @param method
     */
    protected void validate(HttpServletRequest request, Method method) {
        String requestPath = urlPathHelper.getPathWithinApplication(request);

        String token = request.getHeader("token");

        if (StringUtil.isBlank(token)) {
            LOGGER.warn(">>>> No token in request [{}], method [{}]", requestPath, method);
            throw new NoTokenException();
        } else {
            String random = request.getHeader("random");
            String sign = request.getHeader("sign");

            String tokenSecret = null;

            if (StringUtil.equals(token, defaultToken)) {
                tokenSecret = defaultTokenSecret;
                LoginAccountSetter.remove();
            } else {
                LoginAccount loginAccount = LoginAccountSession.get(request);

                if (loginAccount == null) {
                    LOGGER.warn(">>>> No login in request [{}], method [{}]", requestPath, method);
                    throw new NoLoginException();
                } else {
                    tokenSecret = loginAccount.getTokenSecret();

                    if (loginAccount != null) {
                        LoginAccountSetter.put(loginAccount);
                    }
                }
            }

            //TODO 验证签名
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    private void validatePermissions(HttpServletRequest request, String requestPath, Method handlerMethod) {
    }

    /**
     * 获取允许的Functions
     *
     * @param handlerMethod
     * @return
     */
    private Set<String> getAllowFunctions(Method handlerMethod) {
        Set<String> functions = null;

        if (noAuthCaches.contains(handlerMethod)) { // 该method在不需要权限控制的methed缓存中，则不需判断
            return functions;
        }

        functions = this.caches.get(handlerMethod);
        if (functions == null) {
            functions = getDefindFunctions(handlerMethod);
            // 没有配置@UserAuthority
            if (functions.size() == 0) {
                // 该方法或类没有配置UserAuthority,将handlerMethod加入noControlCaches缓存
                noAuthCaches.add(handlerMethod);
            } else {
                // 配置了@UserAuthority，将配置的的权限加入缓存
                this.caches.put(handlerMethod, functions);
            }
        }

        return functions;
    }

    /**
     * 获取方法或类上定义的允许的功能Id
     * <p>
     * author: Evan.Shen<br>
     * version: 2013-2-28 上午10:47:57 <br>
     *
     * @param handlerMethod
     * @return
     */
    private Set<String> getDefindFunctions(Method handlerMethod) {
        LoginAccountAuthority auth1 = null, auth2 = null;
        Set<String> defindFunctions = new HashSet<String>();

        // 取method上的@UserAuthority
        auth1 = AnnotationUtils.getAnnotation(handlerMethod, LoginAccountAuthority.class);
        addFunToSet(auth1, defindFunctions);

        // 取method上没有配置@UserAuthority
        if (defindFunctions.isEmpty()) {
            // 取class上的@UserAuthority
            auth2 = AnnotationUtils.getAnnotation(handlerMethod.getDeclaringClass(), LoginAccountAuthority.class);
            addFunToSet(auth2, defindFunctions);
        }

        return defindFunctions;
    }

    /**
     * <p>
     * author: Evan.Shen<br>
     * version: 2013-2-28 上午11:05:24 <br>
     *
     * @param auth
     * @param defindFunctions
     */
    private void addFunToSet(LoginAccountAuthority auth, Set<String> defindFunctions) {
        if (auth != null && auth.value() != null && auth.value().length > 0) {
            for (String s : auth.value()) {
                defindFunctions.add(s);
            }
        }
    }

    public void setExcludor(Excludor excludor) {
        this.excludor = excludor;
    }

    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    /***/
    public void setDefaultToken(String defaultToken) {
        this.defaultToken = defaultToken;
    }

    /***/
    public void setDefaultTokenSecret(String defaultTokenSecret) {
        this.defaultTokenSecret = defaultTokenSecret;
    }

    /***/
    public void setLoginAccountSession(AbstractLoginAccountSession loginAccountSession) {
        LoginAccountSession = loginAccountSession;
    }
}