/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import java.security.cert.X509Certificate;
import velocity.handler.CertificateHandler;

/**
 *
 * @author Aniket
 */
public class DefaultCertificateHandler implements CertificateHandler {

    @Override
    public boolean authorizeCredentials(X509Certificate cert) {
        return true;
    }

}
