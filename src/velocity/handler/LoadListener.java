/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

/**
 *
 * @author Aniket
 */
public interface LoadListener {

    public void onLoadCompleted();

    public void onLoadCancelled();

    public void onLoadReady();

    public void onLoadFailed();

    public void onLoadScheduled();

    public void onLoadRunning();
}
