## Update 12.22

*@author 谭淑敏*

更新内容：前后端接口

### HttpThread.java

文件中已有详细注释，请结合api文档查看。

由于不同请求的返回格式不同，没有给出统一的解析接口。特别说明一下课程查询的返回，课程查询会返回嵌套Json，即其中的每一个Pair都是 编号:{课程信息} 的形式

例如查询返回两门课，为计算概论和数学分析，则返回的字符串将会是：

{"0":{"name": "计算概论", "credit": "3.0", ...}, "1":{"name": "数学分析", "credit": "4.0", ...}}

### AccountFragment.java

这个界面不由我负责，仅提供了该fragment获取username、院系、专业等信息的方式。这些信息将在注册时保存在本地的SharedPreferences中，叫loginInfo，fragment可以从中读取信息。我在java文件内提供了getValueRefreshed作为接口。

推荐一些信息也可以用SharedPreferences保存在本地，避免频繁访问服务器。



###Course.java

课程类的雏形。

## Update 12.11

*@author 谭淑敏*

更新内容：完成了登陆和注册功能。第一次进入app时会自动跳入登陆界面，登陆成功后回到主界面activity_main

新增文件：

/src/main/java/com.example.electiver/

LoginActivity, MD5Utils, RegisterActivity

/src/main/res/layout/

log_in.xml, register.xml

### LoginActivity

可以验证用户名是否存在、密码是否正确。密码用SharedPreferences保存在xml中，被解包的话会泄露密码，所以使用了MD5加密。

目前还是每次打开app都会需要先登陆的状态，似乎可以用Token实现登陆一次管很久，还需要探索。

### RegisterActivity

会要求选择院系和专业，目前只大致列出了院系，各院系下的专业还没有写进去。另外课表和选课界面写出来之前，这个数据暂时想不到怎么用所以也还没保存。

### test1.py

后端代码，不知道需不需要把包依赖写进来
最新更新，加入了sql安全策略，加入了token机制
