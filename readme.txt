managerIP：集群8180界面的登录ip地址；
username：8180界面登录用户名；
password：界面登录密码

security:集群安全模式

os：操作系统类型

nodeUser：节点登录用户名；
rootKey：用户登录密钥，默认路径为manager节点的/etc/transwarp/transwarp-id_rsa

choose：查询数据字典时查询的数据库：4.6后查inceptor或mysql都行，4.6以前查mysql

className：数据库jdbc连接的驱动名(注意选择对应数据库的classname)
jdbcIP：jdbc连接数据库的IP
port：数据库的端口号
database：查询的数据库中数据字典所在数据库，其中incpeotr的默认为system，而mysql为metastore_inceptorsql1、metastore_inceptorsql2等，具体看对应的inceptor metastore

jdbcUser：jdbc连接的用户名，若查询的为inceptor数据库则在安全为ldap或all的情况下需要设置，而查询为mysql的情况下都需要设置，且用户需要有SDS、TBLS、DBS、TABLE_PARAMS这四张表的查询权限；
jdbcPwd：jdbc连接用户的密码

principal、kuser、keytab、krb5conf为kerberos认证信息，仅在选择数据库为inceptor且安全为kerberos的情况下需要设置

goalPath：最终报告的输出路径

logCheckPath：在各个节点上日志检测脚本以及结果存放的路径

threadNum：整个程序中并行的线程数

configMap：服务配置文件夹映射文件路径，默认为manager节点的/var/lib/transwarp-manager/master/data/data/Service.json