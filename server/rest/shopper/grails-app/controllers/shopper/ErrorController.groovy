package shopper

import grails.converters.JSON

class ErrorController {

    def error404() {
        response.setStatus(response.SC_NOT_FOUND)
        def result = [
            description: 'page not found'
        ]
        render result as JSON
    }

    def error405() {
        response.setStatus(response.SC_METHOD_NOT_ALLOWED)
        def result = [
            description: 'method not allowed'
        ]
        render result as JSON
    }

    def error500() {
        response.setStatus(response.SC_INTERNAL_SERVER_ERROR)
        def exception = request.exception.cause
        def result = [
            description: exception.message
        ]
        render result as JSON
    }
}
