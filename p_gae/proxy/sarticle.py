#encoding=utf-8
import os
import re
import webapp2
from crawler.crawl_tool import *
from tools.web_tool import *

htmltag_re = re.compile('<[^<>]+>')


def s8_gettopics(burl):
    content = geturl(burl)
    for match in re.finditer(r'<a href="read-htm-tid-(\d+).html"[^<>]+>(.*?)</a>', content):
        postid = match.group(1)
        title = match.group(2)
        title = htmltag_re.sub('', title)
        title = title.strip()
        yield postid, title


def s8_getarticles(purl):
    content = geturl(purl)
    for match in re.finditer(r'<div class="f14"[^<>]+>(.+?)</div>', content, re.S):
        body = match.group(1)
        if len(body) < 500:
            continue
        yield body.decode('utf-8')


class ArticleList(webapp2.RequestHandler):
    def get(self, bid, page):
        burl = 'http://sex8.cc/thread-htm-fid-%s-page-%s.html' % (bid, page)
        #burl = 'http://cnbbs6.com/thread-htm-fid-%s-page-%s.html' % (bid, page)
        alist = []
        for pid, title in s8_gettopics(burl):
            alist.append({'pid':pid, 'title':title.decode('utf-8')})

        template_values = {
            'bid' : bid,
            'alist': alist,
            'page': int(page),
        }
        show_html(self.response, 's8/article_list', template_values)


class Article(webapp2.RequestHandler):
    def get(self, pid, page):
        purl = 'http://sex8.cc/read-htm-tid-%s-page-%s.html' % (pid, page)
        #purl = 'http://cnbbs6.com/read-htm-tid-%s-page-%s.html' % (pid, page)
        plist = []
        for body in s8_getarticles(purl):
            plist.append(body)

        template_values = {
            'pid' : pid,
            'plist': plist,
            'page': int(page),
            }
        show_html(self.response, 's8/article', template_values)

class Index(webapp2.RequestHandler):
    def get(self):
        template_values = {
            'blist': [
                {'bid':51, 'name':u'长篇连载'},
                {'bid':46, 'name':u'现代情感'},
                {'bid':49, 'name':u'校园春色'},
                {'bid':47, 'name':u'人妻乱伦'},
                {'bid':50, 'name':u'武侠玄幻'},

            ],
            }
        show_html(self.response, 's8/index', template_values)