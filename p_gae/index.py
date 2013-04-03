#encoding=utf-8
import webapp2
from action import *
from crawler import *
from proxy import sarticle

class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('Hello, Google App Engine')


app = webapp2.WSGIApplication([('/', MainPage),
                            ('/s8/b/(\d+)/(\d+)', sarticle.ArticleList),
                            ('/s8/p/(\d+)/(\d+)', sarticle.Article),
                            ('/s8', sarticle.Index),
                            ],debug=True)