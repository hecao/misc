#coding=utf8
import urllib2
import re

def geturl(url):
    response = urllib2.urlopen(url, timeout = 10)
    try:
        content = response.read()
        return content
    finally:
        response.close()