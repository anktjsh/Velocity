/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import java.security.cert.X509Certificate;

/**
 *
 * @author Aniket
 */
public interface CertificateHandler {

    public boolean authorizeCredentials(X509Certificate cert);
}
