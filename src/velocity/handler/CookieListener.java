/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import velocity.cookie.Cookie;

/**
 *
 * @author Aniket
 */
public interface CookieListener {

    public boolean cookieReceived(Cookie cookie);
}
