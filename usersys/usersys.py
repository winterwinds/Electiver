from flask import Flask
from flask import make_response
from flask import request
from flask_script import Manager
import os
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
basedir=os.path.abspath(os.path.dirname(__file__))
app.config['SQLALCHEMY_DATABASE_URI']='mysql://username:password@localhost/database'
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN']=True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS']=True
userdb=SQLAlchemy(app)
manager=Manager(app)
@app.route('/')
def test():
    return '服务器正常运行'

class userInfoTable(userdb.Model):
    __tablename__='userInfo'
    id=userdb.Column(userdb.Integer,primary_key=True)
    username=userdb.Column(userdb.String(32),unique=True)
    password=userdb.Column(userdb.String(32))
    email=userdb.Column(userdb.String(32))
    rk=userdb.Column(userdb.String(4))
    def __repr__(self):
        if self.rk == '2':
            return 'name is '+self.username+', password is '+self.password+', email is '+self.email+', rank is user'
        elif self.rk == '1':
            return 'name is '+self.username+', password is '+self.password+', email is '+self.email+', rank is administrator'

# #此方法处理用户登录 返回码为0无注册 返回码为1密码错误
# @app.route('/user',methods=['POST'])
# def check_user():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
#         if passwordRight.__len__() != 0:
#             return '登录成功'
#         else:
#             return '1'
#     else:
#         return '0'


#此方法处理用户登录 返回码为0无注册 返回码为1密码错误
# @app.route('/user',methods=['POST'])
def check_user(username,password):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        passwordRight = userInfoTable.query.filter_by(username=username,password=password).all()
        if passwordRight.__len__() != 0:
            return '登录成功'
        else:
            return '1'
    else:
        return '0'


# #此方法处理管理员登录 返回码为0无注册 返回码为1密码错误 返回码为2无管理员权限
# @app.route('/admin',methods=['POST'])
# def check_admin():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
#         if passwordRight.__len__() != 0:
#             userinfo = userInfoTable.query.filter(userInfoTable.username==request.form['username']).first()
#             if userinfo.rk == '1':
#                 return '登录成功'
#             else:
#                 return '2'
#         else:
#             return '1'
#     else:
#         return '0'


#此方法处理管理员登录 返回码为0无注册 返回码为1密码错误 返回码为2无管理员权限
# @app.route('/admin',methods=['POST'])
def check_admin(username,password):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        passwordRight = userInfoTable.query.filter_by(username=username,password=password).all()
        if passwordRight.__len__() != 0:
            userinfo = userInfoTable.query.filter(userInfoTable.username == username).first()
            if userinfo.rk == '1':               
                return '管理员登录成功'
            else:
                return '2'
        else:
            return '1'
    else:
        return '0'

# #此方法处理查看用户信息 返回码为0无注册 返回码为1密码错误
# @app.route('/userinfo',methods=['POST'])
# def user_info():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
#         if passwordRight.__len__() != 0:
#             return passwordRight
#         else:
#             return '1'
#     else:
#         return '0'


#此方法处理查看用户信息 返回码为0无注册 返回码为1密码错误
# @app.route('/userinfo',methods=['POST'])
def user_info(username,password):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        passwordRight = userInfoTable.query.filter_by(username=username,password=password).all()
        if passwordRight.__len__() != 0:
            return passwordRight
        else:
            return '1'
    else:
        return '0'



# #此方法处理查看全部用户信息 需进行管理员登录成功 
# @app.route('/userinfoall',methods=['POST'])
# def user_info_all():
#     userinfo = userInfoTable.query.all()    
#     return userinfo
       

#此方法处理查看全部用户信息 需进行管理员登录成功 
# @app.route('/userinfoall',methods=['POST'])
def user_info_all():
    userinfo = userInfoTable.query.all()    
    return userinfo
       


# #此方法处理修改密码 返回码为0无注册 返回码为1密码错误
# @app.route('/alter',methods=['POST'])
# def alter():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         passwordRight = userInfoTable.query.filter_by(username=request.form['username'],password=request.form['password']).all()
#         if passwordRight.__len__() != 0:
#             userinfo = userInfoTable.query.filter(userInfoTable.username == request.form['username']).first()
#             userinfo.password=request.form['new_password']
#             userdb.session.commit()
#             return '修改成功'
#         else:
#             return '1'
#     else:
#         return '0'


#此方法处理修改密码 返回码为0无注册 返回码为1密码错误
# @app.route('/alter',methods=['POST'])
def alter(username,password,new_password):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        passwordRight = userInfoTable.query.filter_by(username=username,password=password).all()
        if passwordRight.__len__() != 0:
            userinfo = userInfoTable.query.filter(userInfoTable.username == username).first()
            userinfo.password=new_password
            userdb.session.commit()
            return '修改成功'
        else:
            return '1'
    else:
        return '0'



# #此方法处理管理员修改密码 返回码为0无注册 
# @app.route('/alter_admin',methods=['POST'])
# def alter_admin():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         userinfo = userInfoTable.query.filter(userInfoTable.username == request.form['username']).first()
#         userinfo.password=request.form['new_password']
#         userdb.session.commit()
#         return '管理员修改密码成功'
#     else:
#         return '0'


#此方法处理管理员修改密码 返回码为0无注册 
# @app.route('/alter_admin',methods=['POST'])
def alter_admin(username,new_password):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        userinfo = userInfoTable.query.filter(userInfoTable.username == username).first()
        userinfo.password=new_password
        userdb.session.commit()
        return '管理员修改密码成功'
    else:
        return '0'


# #此方法处理管理员提升权限 返回码为0无注册 
# @app.route('/rank_admin',methods=['POST'])
# def rank_admin():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         userinfo = userInfoTable.query.filter(userInfoTable.username == request.form['username']).first()
#         userinfo.rk='1'
#         userdb.session.commit()
#         return '管理员修改权限成功'
#     else:
#         return '0'


#此方法处理管理员修改密码 返回码为0无注册 
# @app.route('/rank_admin',methods=['POST'])
def rank_admin(username):
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        userinfo = userInfoTable.query.filter(userInfoTable.username == username).first()
        userinfo.rk='1'
        userdb.session.commit()
        return '管理员修改权限成功'
    else:
        return '0'

# #此方法处理用户删除 返回码0无此用户
# @app.route('/delete',methods=['POST'])
# def delete_user():
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() == 0: # 判断是否已被注册
#         return '0'
#     userinfo = userInfoTable.query.filter(userInfoTable.username == request.form['username']).first()
#     userdb.session.delete(userinfo)
#     userdb.session.commit()
#     return '删除成功'


#此方法处理用户删除 返回码0无此用户
@app.route('/delete',methods=['POST'])
def delete_user(username):
    userdb.create_all()
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() == 0: # 判断是否已被注册
        return '0'
    userinfo = userInfoTable.query.filter(userInfoTable.username == username).first()
    userdb.session.delete(userinfo)
    userdb.session.commit()
    return '删除成功'



# #此方法处理用户注册
# @app.route('/register',methods=['POST'])
# def register():
#     userdb.create_all()
#     # print(2)
#     have_registed = userInfoTable.query.filter_by(username=request.form['username']).all()
#     if have_registed.__len__() != 0: # 判断是否已被注册
#         return '0'
#     userInfo=userInfoTable(username=request.form['username'],password=request.form['password'],
#             email=request.form['email'],rk='2')
#     userdb.session.add(userInfo)
#     userdb.session.commit()
#     return '注册成功'


# #此方法处理用户注册
# @app.route('/register',methods=['POST'])
def register(username,password,email):
    userdb.create_all()
    have_registed = userInfoTable.query.filter_by(username=username).all()
    if have_registed.__len__() != 0: # 判断是否已被注册
        return '0'
    userInfo=userInfoTable(username=username,password=password,
            email=email,rk='2')
    userdb.session.add(userInfo)
    userdb.session.commit()
    return '注册成功'

if __name__ == '__main__':
    # manager.run()
    #6之后的操作续管理员登录成功
    command=input("输入操作号：1注册,2登录,3查看信息,4修改密码,5管理员登录,6查看所有用户信息,7删除用户,8管理员修改密码,9管理员修改权限")
    if command == '1':
        username=input("username:")
        password=input('password:')
        email=input("email:")
        rs=register(username,password,email)
        print(rs)
    elif command == '2':
        username=input("username:")
        password=input('password:')
        rs=check_user(username,password)
        print(rs)
    elif command == '3':
        username=input("username:")
        password=input('password:')
        rs=user_info(username,password)
        print(rs)
    elif command == '4':
        username=input("username:")
        password=input('password:')
        new_password=input('new_password:')
        rs=alter(username,password,new_password)
        print(rs)
    elif command == '5':
        username=input("username:")
        password=input('password:')
        rs=check_admin(username,password)
        print(rs)
    elif command == '6':
        rs=user_info_all()
        print(rs)
    elif command == '7':
        username=input("username:")
        rs=delete_user(username)
        print(rs)
    elif command == '8':
        username=input("username:")
        new_password=input('new_password:')
        rs=alter_admin(username,new_password)
        print(rs)
    elif command == '9':
        username=input("username:")
        rs=rank_admin(username)
        print(rs)
         