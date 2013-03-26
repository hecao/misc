#encoding=utf-8

import webapp2
from db.post_db import *
from db.comment_db import *
from tools.web_tools import *
from datetime import datetime,timedelta
from google.appengine.api import memcache
from google.appengine.api import users
import json

class CommentList(webapp2.RequestHandler):
    def get(self, postid, pagenum = '1'):
        postid = int(postid)
        try:
            page = int(pagenum)
        except Exception, ex:
            page = 1

        post = Post.getpost(postid)
        count = post.commentCount
        if count is None:
            count = 0
        cpagesize = Config()["commentnumperpage"]
        commentlist = Comment.get_commentlist_bypost(postid, (page-1)*cpagesize, cpagesize)

        isadmin = users.is_current_user_admin()
        result = {"count":count,
                  "page":page,
                  "pagecount":(count-1)/cpagesize + 1,
                  "clist":[comment.to_dict(isadmin) for comment in commentlist]
        }
        show_json(self.response, result)

class CommentInsert(webapp2.RequestHandler):
    def post(self):
        postid = int(self.request.get('postid'))
        user =  users.get_current_user()
        if not user:
            captcha = self.request.get('code')
            seq = self.request.get('seq')
            code = memcache.get("captcha#"+seq)
            memcache.delete("captcha#"+seq)
            if captcha is None or captcha.strip().upper() != code:
                # 验证码错误
                show_json(self.response, {'state':1, 'msg':'验证码错误'})
                return
        comment = Comment()

        post = Post.getpost(postid)
        if post is None:
            show_json(self.response, {'state':-1, 'msg':'不存在的文章'})
            return

        # TODO: escape html
        comment.postId = postid
        comment.content = self.request.get('content')
        comment.username = self.request.get('username')
        comment.email = self.request.get('email')
        comment.homepage = self.request.get('homepage')
        comment.ip = self.request.remote_addr
        comment.date = datetime.today()

        replyto = self.request.get('replyto')
        if replyto:
            pcomment = Comment.getcomment(int(replyto))
            pcomment = pcomment.to_pdict()
            if pcomment:
                comment.parentContent = json.dumps(pcomment)
        if not comment.email:
            comment.email = "default@default.com"
        if comment.homepage and not comment.homepage.startswith('http'):
            comment.homepage = 'http://' + comment.homepage

        Comment.savecomment(comment)
        if post.commentCount is None:
            post.commentCount = 0
        post.commentCount += 1
        Post.savepost(post)
        show_json(self.response, {'state':0, 'msg':''})

class CommentDelete(webapp2.RequestHandler):
    def post(self):
        if not users.is_current_user_admin():
            return
        commentid = int(self.request.get('commentid'))
        comment = Comment.getcomment(commentid)
        postid = comment.postId
        Comment.deletecomment(comment)
        post = Post.getpost(postid)
        if post.commentCount is None:
            post.commentCount = 0
        post.commentCount -= 1
        if post.commentCount < 0:
            post.commentCount = 0
        Post.savepost(post)
        show_json(self.response, {'state':0})