__author__ = 'dongliu'

import jinja2
import os

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.join(os.path.dirname(__file__), '../templates')))

def show_html(response, template, template_values):
    template = jinja_environment.get_template(template + ".html")
    response.headers['Content-Type'] = 'text/html'
    response.out.write(template.render(template_values))