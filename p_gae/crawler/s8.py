#encoding=utf-8

import hashlib
import re
import webapp2
from db.resourcedb import *
from crawl_tool import *


def getTitleFromQvod(link):
    begin = link.find('|')
    begin = link.find('|', begin + 1)
    end = link.rfind('|')
    if 0 < begin < end:
        return link[begin+1:end]
    else:
        return ''


def s8_getids(burl):
    content = geturl(burl)
    begin = content.find('普通主题</td></tr>')
    end = content.find('<tr><td colspan="6" class="f_one"')
    content = content[begin:end]
    for match in re.finditer(r'<a name=(\d+)', content):
        postid = match.group(1)
        yield postid


class QvodCrawler(webapp2.RequestHandler):
    """
    crawl s8 qvod.
    """
    def get(self):
        self.response.headers["Content-Type"] = "text/plain;charset=utf8"
        self.response.write("Get list.")
        burl = 'http://www.sex8.cc/thread-htm-fid-353-page-1.html'
        for postid in s8_getids(burl):
            self.response.write(postid)
            try:
                self.processPost(postid)
            except Exception,ex:
                print ex


    def processPost(self, postid):
        """
        check one post.
        """
        # if post has been processed.
        if Resource.exist_referer(postid):
            return

        postUrl = 'http://www.sex8.cc/read-htm-tid-%s.html' % postid
        content = geturl(postUrl)
        for match in re.finditer(r"VALUE='(qvod://[^<>']+)'", content):
            qvodLink = match.group(1).decode('utf-8')
            break
        else:
            return
        md5key = hashlib.md5(qvodLink.encode('utf-8')).hexdigest().upper()

        # if link exists
        if Resource.get_by_id(md5key) is not None:
            return

        resource = Resource(key_name = md5key)
        resource.scheme = 'qvod'
        resource.link = qvodLink
        resource.title = getTitleFromQvod(qvodLink)
        resource.referer = postid
        if resource.title == '' :
            resource.title = qvodLink
        resource.put()