# Evan开发库 

- cache 缓存
- client 客户端
- orm 关系型数据库
- redis 
- rocketmq
- oauth 鉴权 
- utils 
- web web服务端


## 1.3.3
1、fix 修复HttpUtil获取remoteAddr问题。由于多次反向代理后会有多个ip值，第一个ip才是真实ip，修复之后只返回第一个

## 1.3.2
1、fix 修复AESUtil解密时NumberFormatException没有处理的问题

## 1.3.1
1、fix 对于请求是默认token的，应删除ThreadLocal的保存的会话

## 1.3
1、增加通用查询参数`GeneralQueryParam`

## 1.2
1、增加oauth鉴权

## 1.1 
1、增加`RestListResponse`列表结果、`RestPageResponse`分页结果


