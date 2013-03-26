#encoding=utf-8
import urllib

import jinja2
import os
import webapp2
from db.resourcedb import *

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.join(os.path.dirname(__file__), '../templates')))


# not use.
def qvodEncode(link):
    begin = link.find('|')
    begin = link.find('|', begin+1)
    end = link.rfind('|')
    if 0 < begin < end:
        return link[:begin+1] + urllib.quote(link[begin+1:end].encode('utf-8')) + '|'
    else:
        return link
jinja_environment.globals['qvodEncode'] = qvodEncode

# url encode fuc for jinja
def urlEncode(link):
    return urllib.quote(link.encode('utf-8'))
jinja_environment.globals['urlEncode'] = urlEncode

class QvodList(webapp2.RequestHandler):
    """
    show qvod lists
    """
    def get(self):
        try:
            page = int(self.request.get('page'))
        except Exception, ex:
            page = 1
        pagesize = 20

        resources = Resource.get_resourcelist(page, pagesize)
        template_values = {
            'resources': resources,
            'page': page,
        }
        template = jinja_environment.get_template('qvod_list.html')
        self.response.headers['Content-Type'] = 'text/html'
        self.response.out.write(template.render(template_values))


class QvodPlay(webapp2.RequestHandler):
    """
    page to mack qvod for android work.
    """
    def get(self):
        key = self.request.get('key')
        resource = Resource.get_by_id(key)
        template_values = {
            'resource': resource,
        }
        template = jinja_environment.get_template('qvod_play.html')
        self.response.headers['Content-Type'] = 'text/html'
        self.response.out.write(template.render(template_values))
