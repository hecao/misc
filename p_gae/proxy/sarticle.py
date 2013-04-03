#encoding=utf-8
import os
import re
import webapp2
from crawler.crawl_tool import *
from tools.web_tool import *

htmltag_re = re.compile('<[^<>]+>')

#host = "cnbbs6.com"
host = "sex8.cc"

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


def s8_getqvod(purl):
        content = geturl(purl)
        for match in re.finditer(r"VALUE='(qvod://[^<>']+)'", content):
            qvodLink = match.group(1).decode('utf-8')
            return qvodLink
        else:
            return "not found"


class ArticleList(webapp2.RequestHandler):
    def get(self, bid, page):
        burl = 'http://%s/thread-htm-fid-%s-page-%s.html' % (host, bid, page)
        t = self.request.get('t')
        alist = []
        for pid, title in s8_gettopics(burl):
            alist.append({'pid':pid, 'title':title.decode('utf-8')})

        template_values = {
            'bid' : bid,
            'alist': alist,
            'page': int(page),
            't' : t,
        }
        show_html(self.response, 's8/article_list', template_values)


class Article(webapp2.RequestHandler):
    def get(self, pid, page):
        purl = 'http://%s/read-htm-tid-%s-page-%s.html' % (host, pid, page)
        t = self.request.get('t')
        if t == 'article':
            plist = []
            for body in s8_getarticles(purl):
                plist.append(body)

            template_values = {
                'pid' : pid,
                'plist': plist,
                'page': int(page),
                }
            show_html(self.response, 's8/article', template_values)
        elif t == 'qvod':
            qvodlink = s8_getqvod(purl)
            template_values = {
                'pid' : pid,
                'qvodlink': qvodlink,
                }
            show_html(self.response, 's8/qvod_play', template_values)


class Index(webapp2.RequestHandler):
    def get(self):
        template_values = {
            'blist': [
                {'bid':51, 'name':u'长篇连载', 't':'article'},
                {'bid':46, 'name':u'现代情感', 't':'article'},
                {'bid':49, 'name':u'校园春色', 't':'article'},
                {'bid':47, 'name':u'人妻乱伦', 't':'article'},
                {'bid':50, 'name':u'武侠玄幻', 't':'article'},

                {'bid':353, 'name':u'快播官方', 't':'qvod'},
                {'bid':345, 'name':u'快播会员', 't':'qvod'},
            ],
            }
        show_html(self.response, 's8/index', template_values)