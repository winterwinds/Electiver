# coding=utf-8
from flask import Flask
from flask import Flask, render_template, request, flash,  jsonify, redirect, url_for, session, make_response
from flask_restful import Resource, Api
from flask_restful import reqparse
from flask_sqlalchemy import SQLAlchemy
from flaskext.mysql import MySQL
import json
import os
import datetime
import jwt
import chardet

mysql = MySQL()
app = Flask(__name__)

# MySQL configurations
app.config['MYSQL_DATABASE_USER'] = 'root'
app.config['MYSQL_DATABASE_PASSWORD'] = 'root'
app.config['MYSQL_DATABASE_DB'] = 'coursedb'
app.config['MYSQL_DATABASE_HOST'] = 'localhost'

userdb=SQLAlchemy(app)

mysql.init_app(app)

api = Api(app)

app.config['SQLALCHEMY_DATABASE_URI']='mysql://root:root@localhost/coursedb'
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN']=True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS']=True
userdb=SQLAlchemy(app)

# token key
app.config['SECRET_KEY'] = "\xd3\x82\xa9JD\x0b\xdc?\xb9\x8f2\xa0\xe4\xd2\xa9\xac\x94]/\x1d\r\x841'"

#作者：刘竟择
class userInfoTable(userdb.Model):
	__tablename__='userinfo'
	id=userdb.Column(userdb.Integer,primary_key=True)
	username=userdb.Column(userdb.String(32),unique=True)
	password=userdb.Column(userdb.String(32))
	major=userdb.Column(userdb.String(32))
	department=userdb.Column(userdb.String(32))
	grade=userdb.Column(userdb.String(32))
	rk=userdb.Column(userdb.String(4))
	
	# def __repr__(self):
	#     if self.rk == '2':
	#         return {'name':self.username, 'password':self.password, 'email':self.email, 'rank':self.rk}
	# 		# return 'name is '+self.username+', password is '+self.password+', email is '+self.email+', rank is user'
			
	#     elif self.rk == '1':
	#         # return 'name is '+self.username+', password is '+self.password+', email is '+self.email+', rank is administrator'
	#         return {'name':self.username,'password':self.password,'email':self.email,'rank':self.rk}

# 以下功能均需要用户合法性检测
# 两个接口，一个是检测普通用户合法性，一个是检测管理员用户合法性
# 或者使用session功能

'''
	'cid':'00100873' , #注意是字符串
	'name':'!@#!@$!@$', #支持模糊查询
	'category':'任选', #四个下拉选项之一
	'credit':3.0, #如果输入整数最好转成保留一位小数的形式，比如输入3就转成3.0
	'teacher':'#@$@#$',
	# 对于时间我想做成这种形式，周一至七做成复选框，选择这一天就传入true到字典中，否则传入false。每个复选框下有下拉选项，下拉具体时间段，目前想法是 1-2，3-4，5-6，7-8，10-11，不提供连续三节及以上的课的选项
	'mon':true,
	'moti':'1-2',
	'tue':true,
	'tuti':'1-2',
	'wed':true,
	'weti':'1-2',
	'thu':true,
	'thti':'1-2',
	'fri':true,
	'frti':'1-2',
	'sat':true,
	'sati':'1-2',
	'sun':true,
	'suti':'1-2',
	'depart':'艺术学院' #下拉选项，跟选课网一样
'''

#作者：司昊田
def encode_auth_token(user_id,rk):
	"""
	Generates the Auth Token
	:return: string
	"""
	try:
		payload = {
			'exp': datetime.datetime.utcnow() + datetime.timedelta(days=0, hours=1),
			'iat': datetime.datetime.utcnow(),
			'uid': user_id,
			'rk' : rk
		}
		return jwt.encode(
			payload,
			app.config.get('SECRET_KEY'),
			algorithm='HS256'
		)
	except Exception as e:
		print(e)

#作者：司昊田
def decode_auth_token(auth_token):
	"""
	Decodes the auth token
	:param auth_token:
	:return: dict
	"""
	try:
		payload = jwt.decode(auth_token, app.config.get('SECRET_KEY'))
		return payload
	except jwt.ExpiredSignatureError:
		print 'Signature expired. Please log in again.\n'
		return 2
	except jwt.InvalidTokenError:
		print 'Invalid token. Please log in again.\n'
		return 3

#作者：司昊田
def updatesql(mysql,sql,args):
	flag = True
	conn = mysql.connect()
	cursor = conn.cursor()
	try:
		cursor.execute(sql,args)
		conn.commit()
		print("update success")
	except:
		conn.rollback()
		flag = False
		print("update fail")
	cursor.close()
	conn.close()
	return flag

#作者：司昊田
def querysql(mysql,sql,args):
	conn = mysql.connect()
	cursor = conn.cursor()
	cursor.execute(sql,args)
	results = cursor.fetchall()
	cursor.close()
	conn.close()
	return results

#作者：司昊田
@app.route('/')
def test():
	return '服务器正常运行'

#作者：司昊田
# 查询功能
@app.route('/querycourse',methods=['POST'])
def querycourse():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	cid = request.form.get('cid')
	name = request.form.get('name')
	category = request.form.get('category')
	credit = request.form.get('credit')
	teacher = request.form.get('teacher')
	use_time = request.form.get('use_time')
	mon = request.form.get('mon')
	moti = request.form.get('moti')
	tue = request.form.get('tue')
	tuti = request.form.get('tuti')
	wed = request.form.get('wed')
	weti = request.form.get('weti')
	thu = request.form.get('thu')
	thti = request.form.get('thti')
	fri = request.form.get('fri')
	frti = request.form.get('frti')
	sat = request.form.get('sat')
	sati = request.form.get('sati')
	sun = request.form.get('sun')
	suti = request.form.get('suti')
	depart = request.form.get('depart')

	sql = 'select * from coursetable where '
	args = []
	if cid:
		args.append(cid)
		sql = sql + 'cid=%s and '
	if name:
		args.append(name)
		sql = sql + "name like concat('%%',%s,'%%') and "
	if use_time:
		sql2 = '('
		weekflag = ['mon','tue','wed','thu','fri','sat','sun']
		week = [mon,tue,wed,thu,fri,sat,sun]
		weektime = [moti,tuti,weti,thti,frti,sati,suti]
		for i in range(7):
			if week[i]:
				if weektime[i]:
					sql2 = sql2 + weekflag[i] + '=%s or '
					args.append(weektime[i])
				else :
					sql2 = sql2 + weekflag[i] + ' is not null or '

		if sql2 == '(':
			pass
		else:
			sql2 = sql2[:-4] + ') and '
			sql = sql + sql2
	if depart:
		sql = sql + 'depart=%s and '
		args.append(depart)
	if teacher:
		args.append(teacher)
		sql = sql + 'teacher=%s and '
	if category:
		args.append(category)
		sql = sql + 'category=%s and '
	if credit:
		args.append(credit)
		sql = sql + 'credit=%s and '

	sql = sql[:-4]

	args = tuple(args)
	results = querysql(mysql, sql, args)

	courseinfo={}
	title = ['cid','name','classnum','category','credit','chours','teacher','stucount','totalweek','mon','tue','wed','thu','fri','sat','sun','examtime','note','depart']
	cnt = 0
	for i in list(results):
		oneinfo = dict(zip(title,list(i)))
		leng = len(oneinfo['name'])
		k = 0
		flag = 0
		print(oneinfo['name'])
		#oneinfo['name'] = oneinfo['name'].decode('utf8')
		for k in range(leng-1,-1,-1):
			if u'\u4e00' <= oneinfo['name'][k] <= u'\u9fa5' or oneinfo['name'][k] == '\uFF09':
				break
		oneinfo['name'] = oneinfo['name'][:k+1]
		courseinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(courseinfo)


# 管理员操作，增加、删除、修改
#作者：司昊田
@app.route('/admin/insertinfo',methods=['POST'])
def insertcourse():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"

	cid = request.form.get('cid')
	name = request.form.get('name')
	classnum = int(request.form.get('classnum'))
	category = request.form.get('category')
	credit = float(request.form.get('credit'))
	chours = float(request.form.get('chours'))
	teacher = request.form.get('teacher')
	stucount = int(request.form.get('stucount'))
	totalweek = request.form.get('totalweek')
	mon = request.form.get('mon')
	tue = request.form.get('tue')
	wed = request.form.get('wed')
	thu = request.form.get('thu')
	fri = request.form.get('fri')
	sat = request.form.get('sat')
	sun = request.form.get('sun')
	examtime = request.form.get('examtime')
	note = request.form.get('note')
	depart = request.form.get('depart')
	indexx = [cid,name,category,teacher,totalweek,mon,tue,wed,thu,fri,sat,sun,examtime,note,depart]
	for i in indexx:
		if not i:
			i = ''

	sql = "insert into coursetable(cid,name,classnum,category,credit,chours,teacher,stucount,totalweek,mon,tue,wed,thu,fri,sat,sun,examtime,note,depart) values (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
	args = (cid,name,classnum,category,credit,chours,teacher,stucount,totalweek,mon,tue,wed,thu,fri,sat,sun,examtime,note,depart)
	result = updatesql(mysql, sql, args)
	if result:
		return u'insert success\n'
	else:
		return u'insert fail\n'

#作者：司昊田
#必须提供课程号
@app.route('/admin/deleteinfo',methods=['POST'])
def deletecourse():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"

	cid = request.form.get('cid')

	sql = "delete from coursetable where cid=%s"
	args = (cid)
	result = updatesql(mysql, sql, args)
	if result:
		return u'delete success\n'
	else:
		return u'delete fail\n'

#作者：司昊田
@app.route('/admin/updateinfo',methods=['POST'])
def updatecourse():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"
		
	cid = request.form.get('cid')
	name = request.form.get('name')
	classnum = int(request.form.get('classnum'))
	category = request.form.get('category')
	credit = float(request.form.get('credit'))
	chours = float(request.form.get('chours'))
	teacher = request.form.get('teacher')
	stucount = int(request.form.get('stucount'))
	totalweek = request.form.get('totalweek')
	mon = request.form.get('mon')
	tue = request.form.get('tue')
	wed = request.form.get('wed')
	thu = request.form.get('thu')
	fri = request.form.get('fri')
	sat = request.form.get('sat')
	sun = request.form.get('sun')
	examtime = request.form.get('examtime')
	note = request.form.get('note')
	depart = request.form.get('depart')

	sql = "update coursetable set name=%s,classnum=%s,category=%s,credit=%s,chours=%s,teacher=%s,stucount=%s,totalweek=%s,mon=%s,tue=%s,wed=%s,thu=%s,fri=%s,sat=%s,sun=%s,examtime=%s,note=%s,depart=%s where cid=%s"
	args = (name,classnum,category,credit,chours,teacher,stucount,totalweek,mon,tue,wed,thu,fri,sat,sun,examtime,note,depart,cid)

	result = updatesql(mysql, sql, args)
	if result:
		return u'update success\n'
	else:
		return u'update fail\n'


#作者：司昊田
@app.route('/comment/querybyuid',methods=['GET'])
def querycommentbyuid():
	auth_token = request.args.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']

	sql = "select comment from commenttable where id=%s"
	args = (uid)
	results = querysql(mysql, sql, args)
	allinfo = {}
	cnt = 0
	for i in list(results):
		allinfo[str(cnt)] = list(i)[0]
		cnt += 1
	return json.dumps(allinfo)

#作者：司昊田
@app.route('/comment/querybycid',methods=['GET'])
def querycommentbycid():
	auth_token = request.args.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.args.get('cid')

	sql = "select comid,comment from commenttable where cid=%s"
	args = (cid)
	results = querysql(mysql, sql, args)


	commentinfo = {}
	cnt = 0
	for i in list(results):
		commentinfo[str(cnt)] = list(i)
		cnt += 1
	return json.dumps(commentinfo)

#作者：司昊田
@app.route('/comment/submitcomm',methods=['POST'])
def submitcomment():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')
	comment = request.form.get('comment')

	sql = "insert into commenttable(id,cid,comment) values (%s,%s,%s)"
	args = (uid,cid,comment)
	print sql

	result = updatesql(mysql, sql, args)
	if result:
		return u'insert success\n'
	else:
		return u'insert fail\n'

#作者：司昊田
@app.route('/admin/deletecomment',methods=['POST'])
def admindeletecomment():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"

	comid = int(request.form.get('comid'))

	sql = "delete from commenttable where comid=%s"
	args = (comid)
	print sql

	result = updatesql(mysql, sql, args)
	if result:
		return u'delete success\n'
	else:
		return u'delete fail\n'
#作者：司昊田
@app.route('/comment/deletecomment',methods=['POST'])
def deletecomment():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	comid = int(request.form.get('comid'))

	sql = "delete from commenttable where id=%s and comid=%s"
	args = (uid,comid)
	print sql
	result = updatesql(mysql, sql, args)
	if result:
		return u'delete success\n'
	else:
		return u'delete fail\n'


#作者：司昊田
@app.route('/ddl/queryddl',methods=['POST'])
def queryddl():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')
	if cid:
		sql = "select ddlid,id,ddlcontent,ddltime,ddlstate from ddltable where id=%s and cid=%s order by ddltime"
		args = (uid,cid)
		print sql
	else:
		sql = "select ddlid,id,ddlcontent,ddltime,ddlstate from ddltable where id=%s order by ddltime"
		args = (uid)
		print sql

	results = querysql(mysql, sql, args)
	allinfo = {}
	cnt = 0
	for i in list(results):
		oneinfo = list(i)
		allinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(allinfo)

#作者：司昊田
@app.route('/ddl/insertddl',methods=['POST'])
def insertddl():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')
	ddlcontent = request.form.get('ddlcontent')
	ddltime = request.form.get('ddltime')
	ddlstate = int(request.form.get('ddlstate'))

	sql = "insert into ddltable (id,cid,ddlcontent,ddltime,ddlstate) values (%s,%s,%s,%s,%s)"
	args = (uid,cid,ddlcontent,ddltime,ddlstate)
	print sql

	result = updatesql(mysql, sql, args)
	if result:
		return u'insert success\n'
	else:
		return u'insert fail\n'

#作者：司昊田
@app.route('/ddl/updatestate',methods=['POST'])
def updatestate():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	ddlid = int(request.form.get('ddlid'))
	ddlstate = int(request.form.get('ddlstate'))

	sql = "update ddltable set ddlstate=%s where ddlid=%s and id=%s"
	args = (ddlstate,ddlid,uid)
	print sql
	result = updatesql(mysql, sql, args)
	if result:
		return u'update success\n'
	else:
		return u'update fail\n'

#作者：司昊田
@app.route('/ddl/updateddlinfo',methods=['POST'])
def updateddlinfo():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	ddlid = int(request.form.get('ddlid'))
	ddlcontent = request.form.get('ddlcontent')
	ddltime = request.form.get('ddltime')

	sql = "update ddltable set ddlcontent=%s , ddltime=%s where ddlid=%s and id=%s"
	args = (ddlcontent,ddltime,ddlid,uid)
	print sql

	result = updatesql(mysql, sql, args)
	if result:
		return u'update success\n'
	else:
		return u'update fail\n'

#作者：司昊田
@app.route('/ddl/deleteddl',methods=['POST'])
def deleteddl():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	ddlid = int(request.form.get('ddlid'))

	sql = "delete from ddltable where id=%s and ddlid=%s"
	args = (uid,ddlid)
	print sql

	result = updatesql(mysql, sql, args)
	if result:
		return u'delete success\n'
	else:
		return u'delete fail\n'

#作者：司昊田
@app.route('/usrcou/insertusrcou',methods=['POST'])
def insertusrcou():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')

	sql = "insert into usrcoutable (id,cid) values (%s,%s)"
	args = (uid,cid)
	print sql
	result = updatesql(mysql, sql, args)
	if result:
		return u'insert success\n'
	else:
		return u'insert fail\n'

#作者：司昊田
@app.route('/usrcou/queryusrcou',methods=['POST'])
def queryusrcou():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	sql = "select cid from usrcoutable where id=%s"
	args = (uid)
	print sql
	
	allinfo = {}

	results = querysql(mysql, sql, args)
	cnt = 0
	for i in list(results):
		oneinfo = list(i)
		allinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(allinfo)


#作者：刘竟择
@app.route('/usrcou/deleteusrcou',methods=['POST'])
def deleteusrcou():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')

	sql1="DELETE FROM usrcoutable WHERE id = %s AND cid = %s"
	args = (uid,cid)
	print sql1
	result1 = updatesql(mysql, sql1, args)
	sql2="DELETE FROM ddltable WHERE id = %s AND cid = %s"
	args = (uid,cid)
	print sql2
	result2 = updatesql(mysql, sql2, args)
	if result1 and result2:
		return u'delete success\n'
	elif result1 and (result2==False):
		return u'delete ddl fail\n'
	elif result2 and (result1==False):
		return u'delete usrcoutable fail\n'
	else:
		return u'delete ddl and usrcoutable both fail\n'


#作者：刘竟择
@app.route('/usrcou/oldinsertusrcou',methods=['POST'])
def oldinsertusrcou():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')
	print(uid,cid)
	sql = "insert into oldusrcoutable (id,cid) values (%s,%s)"
	args = (uid,cid)
	print sql
	result = updatesql(mysql, sql, args)
	if result:
		return u'insert success\n'
	else:
		return u'insert fail\n'

#作者：刘竟择
@app.route('/usrcou/oldqueryusrcou',methods=['POST'])
def oldqueryusrcou():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	sql = "select cid from oldusrcoutable where id=%s"
	args = (uid)
	print sql
	
	allinfo = {}

	results = querysql(mysql, sql, args)
	cnt = 0
	for i in list(results):
		oneinfo = list(i)
		allinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(allinfo)


#作者：刘竟择
@app.route('/usrcou/olddeleteusrcou',methods=['POST'])
def olddeleteusrcou():
	# 加入身份认证
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'

	uid = auth['uid']
	cid = request.form.get('cid')

	sql1="DELETE FROM oldusrcoutable WHERE id = %s AND cid = %s"
	args = (uid,cid)
	print sql1
	result1 = updatesql(mysql, sql1, args)
	
	if result1 :
		return u'delete success\n'
	else:
		return u'delete oldusrcoutable fail\n'

#作者：刘竟择
#此方法处理用户登录
@app.route('/user/enroll',methods=['POST'])
def check_user():
	result = {}
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
		if passwordRight.__len__() != 0:
			userinfo = userInfoTable.query.filter(userInfoTable.username==request.form['username']).first()
			result['token'] = encode_auth_token(userinfo.id,userinfo.rk)
			result['major'] = userinfo.major
			result['department'] = userinfo.department
			result['grade'] = userinfo.grade
			return json.dumps(result)
		else:
			return 'password wrong'
	else:
		return 'this username not exist'


#作者：刘竟择
#此方法处理管理员登录 
@app.route('/admin/enroll',methods=['POST'])
def check_admin():
	result = {}
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
		if passwordRight.__len__() != 0:
			userinfo = userInfoTable.query.filter(userInfoTable.username==request.form['username']).first()
			if userinfo.rk == '1':
				result['token'] = encode_auth_token(userinfo.id,userinfo.rk)
				result['major'] = userinfo.major
				result['department'] = userinfo.department
				result['grade'] = userinfo.grade
				return json.dumps(result)
			else:
				return 'not admin'
		else:
			return 'password wrong'
	else:
		return 'this username not exist'

#作者：刘竟择
#此方法处理查看用户信息 
@app.route('/userinfo/one',methods=['POST'])
def user_info():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	# if rk == 2:
	#     return u"Permission Denied"

	info = userInfoTable.query.filter_by(id=uid).first()
	result={}

	# if have_registed.__len__() != 0: # 判断是否已被注册
	#     passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
	#     if passwordRight.__len__() != 0:

	userobject=info.__dict__
	result['success']='success query'
	for i in userobject.keys():
		if i == "_sa_instance_state":
			continue
		elif isinstance(userobject[i],str):
			result[i] = userobject[i]
		else:
			result[i] = str(userobject[i])
	return json.dumps(result)
	#     else:
	#         result['success']='password wrong'
	#         return json.dumps(result)
	# else:
	#     result['success']='username is not exist'
	#     return json.dumps(result)

#作者：刘竟择
#此方法处理查看全部用户信息 需进行管理员登录成功 
@app.route('/userinfo/all',methods=['POST'])
def user_info_all():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"


	userinfo = userInfoTable.query.all()
	outinfo={}
	cnt=0
	for i in userinfo:
		dicti=i.__dict__
		oneinfo={}
		for j in dicti.keys():
			if j == "_sa_instance_state":
				continue
			elif isinstance(dicti[j],str):
				oneinfo[j] = dicti[j]
			else:
				oneinfo[j] = str(dicti[j])
		outinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(outinfo)

#作者：刘竟择
#此方法处理修改密码
@app.route('/user/alter',methods=['POST'])
def alter():
	# auth_token = request.form.get('token')
	# auth = decode_auth_token(auth_token)
	# if auth == 2:
	# 	return u'Signature expired. Please log in again.\n'
	# if auth == 3:
	# 	return u'Invalid token. Please log in again.\n'
	# uid = auth['uid']
	# rk = auth['rk']
	# if rk == 2:
	# 	return u"Permission Denied"
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		passwordRight = userInfoTable.query.filter_by(username=request.form['username']).all()
		if passwordRight.__len__() != 0:
			userinfo = userInfoTable.query.filter(userInfoTable.username == request.form['username']).first()
			userinfo.password=request.form['new_password']
			userdb.session.commit()
			return 'change password success'
		else:
			return 'password wrong'
	else:
		return 'this username not exist'


	# have_registed = userInfoTable.query.filter_by(id=uid).all()
	# if have_registed.__len__() != 0: # 判断是否已被注册
	# 	passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
	# 	if passwordRight.__len__() != 0:
	# userinfo = userInfoTable.query.filter(userInfoTable.id == uid).first()
	# userinfo.password=request.form['new_password']
	# userdb.session.commit()
	# return 'change password success'
	# 	else:
	# 		return 'password wrong'
	# else:
	# 	return 'this username not exist'

#作者：刘竟择
#此方法处理管理员修改密码  
@app.route('/admin/alter',methods=['POST'])
def alter_admin():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"
	# have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	# if have_registed.__len__() != 0: # 判断是否已被注册
	userinfo = userInfoTable.query.filter(userInfoTable.id == uid).first()
	userinfo.password=request.form['new_password']
	userdb.session.commit()
	return 'admin change password success'
	# else:
	# 	return 'this username not exist'

#作者：刘竟择
#此方法处理管理员提升权限  
@app.route('/admin/rank',methods=['POST'])
def rank_admin():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"
	# have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	# if have_registed.__len__() != 0: # 判断是否已被注册
	userinfo = userInfoTable.query.filter(userInfoTable.id == uid).first()
	userinfo.rk='1'
	userdb.session.commit()
	return 'admin change rank success'
	# else:
	# 	return 'this username not exist'

#作者：刘竟择
#此方法处理用户删除 
@app.route('/admin/delete',methods=['POST'])
def delete_user():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	if rk == 2:
		return u"Permission Denied"
	# have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	# if have_registed.__len__() == 0: # 判断是否已被注册
	# 	return 'this username not exist'
	userinfo = userInfoTable.query.filter(userInfoTable.id == uid).first()
	userdb.session.delete(userinfo)
	userdb.session.commit()
	return 'delete success'

#作者：刘竟择
#此方法处理用户注册
@app.route('/user/register',methods=['POST'])
def register():
	userdb.create_all()
	# print(2)
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		return 'this name have existed'
	userInfo=userInfoTable(username=request.form['username'],password=request.form['password'],major=request.form['major'],department=request.form['department'],grade=request.form['grade'],rk='2')
	userdb.session.add(userInfo)
	userdb.session.commit()
	return 'register successs'

#作者：刘竟择
# @app.route('/usrcou/queryallusrcou',methods=['POST'])
def queryallusrcou():

	sql = "select id from oldusrcoutable"
	
	# print sql
	
	allinfo = {}
	args = None
	result0 = querysql(mysql, sql, args)
	result1 = list(result0)
	result2 = []
	for i in result1:
		# print(i,type(i))
		result2.append(i[0])
	result3 = list(set(result2))
	# print("result3",result3)

	for i in result3:
		sql = "select cid from oldusrcoutable where id=%s"
		args = (i)
		# print(i)
		# print(type(i))
		# print sql
		result4 = querysql(mysql, sql, args)
		# print("result4",result4)
		result5 = list(result4)
		result6 = []
		for j in result5:
			# print(j,type(j))
			result6.append(j[0])
		result7 = list(set(result6))
		# print("result7",result7)
		allinfo[i]=result7
		# print("allinfo",allinfo)
	# return json.dumps(allinfo)
	return allinfo






# 课程推荐内容
# 这一部分，算法由王泽楷实现，接口由刘竟择实现
# In[3]:


#--------定义需要的公共变量，写好赋值函数就可以用了
ID = str()
DICTION = dict()
CATEGORY = dict()
REC_NUM = int()
REC_TYPE = list()
RESULT_NUM = int()


# In[4]:


#--------读入数据
#--用户信息数据库
def get_usr_data():
	global DICTION
	DICTION = queryallusrcou()
   

#DICTION['0000'][1]

#作者：刘竟择
#--课程对应的课程类型
def get_CATEGORY(courses):
	allinfo={}
	for i in courses:
		sql = 'select * from coursetable where cid=%s'
		args = (i)
		result = querysql(mysql, sql, args)
		allinfo[i] = result[0][3]
	print("allinfo",allinfo)
	global CATEGORY 
	CATEGORY = allinfo


# In[6]:


#--------读入数据：需要推荐课程的用户ID
def get_usr_ID(uid):
	global ID
	ID  = uid
    

#ID
#--------读入数据：初步推荐的用户数，根据数据集大小确定
def get_REC_NUM():
	global REC_NUM
	get_sum = 3
	REC_NUM = get_sum


#--------读入数据：推荐的课程数，根据需求确定
def get_RESULT_SUM(recom_num):
	global RESULT_NUM
	get_sum = recom_num
	RESULT_NUM = get_sum
  

#--------读入数据：需要推荐的课程类型，根据需求确定
def get_type(type1,type2):
	# print("type1",type1)
	global REC_TYPE
	get_rec_type = [type1,type2]
	REC_TYPE = get_rec_type



# In[7]:

#作者：王泽楷
#--------计算两个用户的相似度
#--传入DIC[uid1], DIC[uid2]
#--计算二者之间的距离，维数以 被推荐人(usr1) 的课程数为准
#--这起到筛查届别的作用，因为与被推荐人课程总数相似的一般都是同届同专业的人
#--而往往课程相似，课程数目更多的一般是同专业更高届的人
#--这使得算法倾向于推荐与usr1同专业同届或更高届的其他人的课程
#--尤其是同届或更高届学生的专业课，这在之后应当被筛除
def cal_similarity(class_list_usr1, class_list_usr2):
	dis_cnt = 0#二者不同课程数
	sim_cnt = 0#二者相同课程数
	for cid_usr1 in class_list_usr1:
		if cid_usr1 in class_list_usr2:
			sim_cnt += 1
		else:
			dis_cnt += 1
	if sim_cnt == len(class_list_usr2):#用户1的课表完全涵盖了用户2的，不需要再推荐
		dis_cnt = 0xffff
	return dis_cnt
#cal_similarity(DICTION['0000'], DICTION['0001'])


# In[8]:

#作者：王泽楷
#--------推荐出最接近的rec_num个人
def sort_key(enum):
	return enum[1]


#作者：王泽楷
def recommend():
	#先将用户按距离排序
	result = list()
	for people in DICTION:
		print("people",people)
		print("DICTION",DICTION)
		result.append([people,cal_similarity(DICTION[ID], DICTION[people])])
	result.sort(key = sort_key)
	return result[:REC_NUM]


# In[9]:

#作者：王泽楷
#--------从推荐出的用户列表中对每个课程出现的次数求和，排除已经选过的和不需要的课程类型，输出结果列表
#--这里当然可以根据用户相近情况加权，但是写起来好麻烦，让我偷下懒吧QAQ
def sum_recommend(usr_list):
	class_cnt_dict = dict()
	for uid in usr_list:
		for cid in DICTION[uid[0]]:
			if cid in DICTION[ID]: continue
			if CATEGORY[cid] not in REC_TYPE: continue
			if cid in class_cnt_dict:
				class_cnt_dict[cid] = class_cnt_dict[cid] + 1
			else:
				class_cnt_dict[cid] = 1
    
	class_cnt_list = list()
	for cid in class_cnt_dict:
		class_cnt_list.append([cid, class_cnt_dict[cid]])
	class_cnt_list.sort(key = sort_key,reverse = True)
	return class_cnt_list[:RESULT_NUM]

#作者：刘竟择
@app.route('/recommend',methods=['POST'])
def recom():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']

	get_usr_ID(uid)
	get_RESULT_SUM(request.args.get('recom_num'))
	get_usr_data()
	
	global DICTION
	usrcouinfo = DICTION
	num = []
	for i in usrcouinfo.keys():
		res = usrcouinfo[i]
		for j in res:
			num.append(j)
	# print(num)
	courses = list(set(num))
	# print("courses",courses)

	get_CATEGORY(courses)
	get_REC_NUM()

	get_type(request.form.get('type1'),request.form.get('type2'))

	result = {}
	lst = sum_recommend(recommend())
	cnt = 0
	title = ['cid','name','classnum','category','credit','chours','teacher','stucount','totalweek','mon','tue','wed','thu','fri','sat','sun','examtime','note','depart']
	for i in lst:
		# print(i)
		cid = i[0]
		sql = 'select * from coursetable where cid = %s'
		args = (cid)
		results = querysql(mysql, sql, args)
		print(results)
		print('*'*100)
		for j in results:
			oneinfo = dict(zip(title,list(j)))
			result[str(cnt)] = oneinfo
			cnt += 1
	return json.dumps(result)



	
	# cnt = 0
	# for i in list(results):
	# 	oneinfo = dict(zip(title,list(i)))
	# 	leng = len(oneinfo['name'])
	# 	k = 0
	# 	flag = 0
	# 	print(oneinfo['name'])
	# 	#oneinfo['name'] = oneinfo['name'].decode('utf8')
	# 	for k in range(leng-1,-1,-1):
	# 		if u'\u4e00' <= oneinfo['name'][k] <= u'\u9fa5' or oneinfo['name'][k] == '\uFF09':
	# 			break
	# 	oneinfo['name'] = oneinfo['name'][:k+1]
	# 	courseinfo[str(cnt)] = oneinfo
	# 	cnt += 1
	# return json.dumps(courseinfo)
# In[ ]:

if __name__ == '__main__':
	app.run(host="0.0.0.0",port=5001)
