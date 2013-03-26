#encoding=utf-8

from google.appengine.ext import ndb

class Resource(ndb.Model):
    scheme = ndb.StringProperty()
    link = ndb.StringProperty(indexed = False)
    title = ndb.StringProperty(indexed = False)
    referer = ndb.StringProperty()
    date = ndb.DateTimeProperty(auto_now_add = True)

    @staticmethod
    def get_resourcelist(page, pagesize):
        """
        get resource list order by date desc
        """
        q = Resource.query()
        q = q.order(Resource.date)
        return q.fetch(offset = (page-1)*pagesize, limit = pagesize)

    @staticmethod
    def exist_referer(referer):
        q = Resource.query()
        q = q.filter(Resource.referer == referer)
        return q.get() is not None