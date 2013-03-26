#encoding=utf-8

from google.appengine.ext import ndb

class ArticleInfo(ndb.Model):
    link = ndb.StringProperty(indexed = False)
    title = ndb.StringProperty()
    referer = ndb.StringProperty()
    date = ndb.DateTimeProperty(auto_now_add = True)

    @staticmethod
    def get_articles(page, pagesize):
        q = ArticleInfo.query()
        q = q.order(ArticleInfo.date)
        return q.fetch(offset = (page-1)*pagesize, limit = pagesize)

    @staticmethod
    def exist_referer(referer):
        q = ArticleInfo.query()
        q = q.filter(ArticleInfo.referer == referer)
        return q.get() is not None

class Article(ndb.Model):
    aid = ndb.IntegerProperty()
    seq = ndb.IntegerProperty()
    content = ndb.BlobProperty()
    date = ndb.DateProperty()

