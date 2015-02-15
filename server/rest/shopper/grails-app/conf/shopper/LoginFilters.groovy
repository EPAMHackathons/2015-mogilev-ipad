package shopper

import grails.converters.JSON

class LoginFilters {

    private final static def EXCLUDED_ACTIONS = ['user': ['login', 'logout'],
                                                 'store': ['index'],
                                                 'error': ['*']]

    def loginService

    def filters = {
        all(controller:'*', action:'*') {
            before = {
                if (loginRequired(controllerName, actionName)) {
                    def uuid = params.token
                    if (!uuid || !loginService.findToken(uuid)) {
                        def result = [
                            desctiption: 'login required'
                        ]
                        response.setStatus(response.SC_FORBIDDEN)
                        render result as JSON
                        return false
                    }


                }
            }
        }
    }

    private def loginRequired(controllerName, actionName = 'index') {
        if (!actionName) {
            actionName = 'index'
        }
        def result = true
        def exludedActions = EXCLUDED_ACTIONS[controllerName]
        if (exludedActions) {
            if (exludedActions[0] == '*' || actionName in exludedActions) {
                result = false
            }
        }
        result
    }
}
