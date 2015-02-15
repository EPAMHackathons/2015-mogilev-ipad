package shopper

import grails.transaction.Transactional
import groovy.time.TimeCategory

@Transactional
class LoginService {

    private List<Token> tokens = []

    public Token getToken(User user) {
        def token = tokens.find {it.userId == user.id}
        if (!token) {
            token = new Token(user)
            tokens << token
        } else {
            token.updateEndDate()
        }
        token
    }

    public Token findToken(String uuid) {
        tokens.find {it.uuid == uuid}
    }

    public static class Token {

        Long userId
        String uuid
        Date endDate

        public Token(User user) {
            this.userId = user.id
            this.uuid = UUID.randomUUID().toString()
            updateEndDate()
        }

        public void updateEndDate() {
            use(TimeCategory) {
                endDate = new Date() + 30.minutes
            }
        }
    }
}
