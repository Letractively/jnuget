/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.aristar.jnuget;

import ru.aristar.jnuget.rss.PackageFeed;
import java.util.List;

/**
 * 
 * @author Unlocker
 */
public interface PackageProvider {
    
    PackageFeed getFeed();
    
}
