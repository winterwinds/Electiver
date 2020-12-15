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
