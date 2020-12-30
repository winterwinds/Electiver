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

class userInfoTable(userdb.Model):
	__tablename__='userinfo'
	id=userdb.Column(userdb.Integer,primary_key=True)
	username=userdb.Column(userdb.String(32),unique=True)
	password=userdb.Column(userdb.String(32))
	email=userdb.Column(userdb.String(32))
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

def querysql(mysql,sql,args):
	conn = mysql.connect()
	cursor = conn.cursor()
	cursor.execute(sql,args)
	results = cursor.fetchall()
	cursor.close()
	conn.close()
	return results

@app.route('/')
def test():
	return '服务器正常运行'

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
		courseinfo[str(cnt)] = oneinfo
		cnt += 1
	return json.dumps(courseinfo)


# 管理员操作，增加、删除、修改

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
	print sql
	result1 = updatesql(mysql, sql1, args)
	sql2="DELETE FROM ddltable WHERE id = %s AND cid = %s"
	args = (uid,cid)
	print sql
	result2 = updatesql(mysql, sql2, args)
	if result1 and result2:
		return u'delete success\n'
	elif result1 and (result2==False):
		return u'delete ddl fail\n'
	elif result2 and (result1==False):
		return u'delete usrcoutable fail\n'
	else:
		return u'delete ddl and usrcoutable both fail\n'


#此方法处理用户登录
@app.route('/user/enroll',methods=['POST'])
def check_user():
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
		if passwordRight.__len__() != 0:
			userinfo = userInfoTable.query.filter(userInfoTable.username==request.form['username']).first()
			return encode_auth_token(userinfo.id,userinfo.rk)
		else:
			return 'this username not exist'
	else:
		return 'password wrong'



#此方法处理管理员登录 
@app.route('/admin/enroll',methods=['POST'])
def check_admin():
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
		if passwordRight.__len__() != 0:
			userinfo = userInfoTable.query.filter(userInfoTable.username==request.form['username']).first()
			if userinfo.rk == '1':
				return encode_auth_token(userinfo.id,userinfo.rk)
			else:
				return 'not admin'
		else:
			return 'password wrong'
	else:
		return 'this username not exist'

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



#此方法处理修改密码
@app.route('/user/alter',methods=['POST'])
def alter():
	auth_token = request.form.get('token')
	auth = decode_auth_token(auth_token)
	if auth == 2:
		return u'Signature expired. Please log in again.\n'
	if auth == 3:
		return u'Invalid token. Please log in again.\n'
	uid = auth['uid']
	rk = auth['rk']
	# if rk == 2:
	# 	return u"Permission Denied"
	# have_registed = userInfoTable.query.filter_by(id=uid).all()
	# if have_registed.__len__() != 0: # 判断是否已被注册
	# 	passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
	# 	if passwordRight.__len__() != 0:
	userinfo = userInfoTable.query.filter(userInfoTable.id == uid).first()
	userinfo.password=request.form['new_password']
	userdb.session.commit()
	return 'change password success'
	# 	else:
	# 		return 'password wrong'
	# else:
	# 	return 'this username not exist'


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


#此方法处理用户注册
@app.route('/user/register',methods=['POST'])
def register():
	userdb.create_all()
	# print(2)
	have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
	if have_registed.__len__() != 0: # 判断是否已被注册
		return 'this name have existed'
	userInfo=userInfoTable(username=request.form['username'],password=request.form['password'],
			email=request.form['email'],rk='2')
	userdb.session.add(userInfo)
	userdb.session.commit()
	return 'register successs'

if __name__ == '__main__':
	app.run(host="0.0.0.0",port=5001)
