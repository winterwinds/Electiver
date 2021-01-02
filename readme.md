## backend content
  environment : flask, python2.7
  运行代码请用

  python final_edition.py

  后端开发由刘竟择和司昊田完成
  各自的函数后面标明了作者
  目前后端程序运行在tmux的session-flask中
  请用 tmux a -t flask进入

## Update 12.27

*@author 谭淑敏*

### Token的保存和获取

HttpThread中的所有请求都需要提供Token，登陆后Token保存在SharedPreferences文件loginInfo中，需要用时请从中取出。

```java
String Token;
SharedPreferences getToken = Context.getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
Token = getToken.getString("Token","null");
//if(Token.equals("null"))说明loginInfo中没有Token，可能是哪里出错了
```



### 已选课程的储存和获取方式

保存有courseInfo文件和timeAvail文件，每次在assistant中选取课程时，在两个SharedPreference中均会保存。

删除课程时需要维护这两个文件的内容，详见后文。

#### courseInfo

courseInfo中保存有一项courseNum，其value值为当前courseInfo中的课程数量（也就是已选课程数量，注意格式是String）。取用方式：

```java
//getSP是activity的方法，fragment或其他类要调用的话需借助context
SharedPreferences getCourse = Context.getSharedPreferences("courseInfo",Context.MODE_PRIVATE);
//注意default值必须为"0"，因为特定时候我会清空所有SP文件，导致courseInfo中并没有courseNum这一项
String courseNum = getCourse.getString("courseNum","0");
```

其中保存的第i个课程的key值为"course"+i，value值为该课程信息的JSON格式字符串（Course类提供JSONString和Course类的互转，详见Course类部分注释）

key值需要自行拼接

```java
//取出的为0号槽的课程信息，如不存在默认设为"null"
String mycourse1 = getCourse.getString("course0","null");
//取出10号槽的课程信息
String mycourse2 = getCourse.getString("course10","null");
```

将课程从已选列表中删除时，务必将该槽的值修改为"null"，不然我添加课程会有问题。然后将courseNum的值-1。

```java
SharedPreferences.Editor editor = getCourse.edit();
//将10号槽的课程移出，保存的值改成"null"
editor.putString("course10","null");
//(String)courseNum是从courseInfo中取出的课程数目（字符串形式）
int newCourseNum = Interer.valueOf(courseNum).intValue()-1;
editor.putString("courseNum",String.valueOf(newCourseNum));
//提交修改
editor.commit();
```

注意，如需要遍历courseInfo中的所有课程，不能使用for(i=0;i<courseNum;i++)，因为某个课程的槽号可能目前的courseNum数。

比如我先选三门课，现在courseInfo的内容为：

```java
<"courseNum","3">
<"course0",courseInfo0>
<"course1",courseInfo1>
<"course2",courseInfo2>
```

然后删除课程0和课程1，courseInfo的内容变为：

```java
<"courseNum","1">
<"course2",courseInfo2>
```

比较合适的方法是遍历map（请自行查询java中遍历map的方法），或者设置一个更大的循环bound。不必担心课程编号会无限制增长，我在增加课程时会从0开始找空槽。比如最大的选课数目不大可能超过20，可以尝试for(i=0;i<20;i++)；

### timeAvail

该SP以时间形式保存已选课程。key值的形式为day+time，一共有7x5=35个时间槽，value值为true/false（字符串，都是小写）。

```java
day的取值："mon","tue","wed","thu","fri","sat","sun"
time的取值："1-2","3-4","5-6","7-8","10-11"
7-9和7-8共用"7-8"，10-11和10-12共用"10-11"
如果key值"mon1-2"的value为true，表示周一1-2节已经有课
```

Course类中保存成员变量timetag1和timetag2，初始值为"null"（字符串），赋值后会自动将上课时间转换成上面的形式。

```java
course0.timetag1=="tue3-4" && course0.timetag2=="fri7-8";
//表示course0的上课时间为周二3-4和周五7-8
course1.timetag1=="thu5-6" && course1.timetag2=="null";
//表示course1的上课时间为周四5-6，一周只有一节课
course2.timetag1=="null" && course2.timetag2=="null";
//course2很可能未初始化
```

删除课程时同样需要维护timeAvail，简单地将课程的timetag的value值置为false

```java
SharedPreferences getTime = Context.getSharedPreferences("timeAvail",Context.MODE_PRIVATE);
SharedPreferences.Editor editor = getTime.edit();
if(!course.GetTimetag1().equals("null")){
  editor.putString(course.GetTimetag1,"false");
}
if(!course.GetTimetag2().equals("null")){
  editor.putString(course.GetTimetag2,"false");
}
editor.commit();
```



### Course类

Course类有且仅有两种初始化方式，请选择其中之一。

```java
//方法一：在构造函数中设置所有成员变量
Course mycourse0 = new Course(String cid, String name, String category, String credit, String teacher, boolean[7]days, String[7]time,String depart);
//方法二（推荐）：用空构造函数，然后用SetAllAttr(String allAttr)设置所有成员变量
//参数是一个JSON格式的字符串，SP中存储的课程信息都是JSON格式字符串，也就是从courseInfo中取出的某课程槽中的字符串可以直接拿来初始化客车鞥
Course mycourse1 = new Course();
String getInfo = mysharedpreferences.getString("course1","null");
//请确保的确取出了信息而不是"null"，否则会抛出JSON异常
if(!getInfo.equals("null")){
  mycourse1.SetAllAttr(getInfo);
}
```

提供将课程转化为JSON字符串的函数

```java
//mycourse是一个已经初始化的Course类实例
String getInfo = mycourse.Course2JSONString()
```

提供检查该课程在某个时间是否有课的函数

```java
//周一1-2有课返回True
Boolean hasCourseonTime = course0.checktime(0,"1-2");
//周四5-6有课返回True
Boolean hasCourseonTime = course0.checktime(3,"5-6");
```

提供检查该课程是否能够加入课表而不与其他课程冲突的函数

```java
boolean ifOktoAdd = course0.ifOktoAddCourse(getContext());
```



### 用户信息的保存和获取

用户信息，包括用户名、年级、专业、院系同样保存在loginInfo中。

Grade, Department,Major, Token由调用login的activity在login结束后保存，UserName由login保存。

获取方式：

```java
SharedPreferences getUserInfo = getSharedPreferences("loginInfo", MODE_PRIVATE);
String userName = getUserInfo.getString("UserName","null");
String Grade = getUserInfo.getString("Grade","null");
String Department = getUserInfo.getString("Department","null");
String Major = getUserInfo.getString("Major","null");
```



### 登陆状态的维护

打开app会向服务器发送一个请求验证当前保存的Token是否依然有效，如果无效的话弹出登陆界面，并清空三个SP文件(loginInfo, courseInfo, timeAvail)。

退出登录也需要清空SP文件。



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
