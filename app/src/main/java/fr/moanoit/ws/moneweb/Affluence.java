package fr.moanoit.ws.moneweb;

import com.android.volley.RequestQueue;

/**
 * Created by bdelbos on 21/10/2016.
 */

public class Affluence {

    protected RequestQueue mRequestQueue = null;

    /**
     * Constructeur privé
     */
    private Affluence() {
    }

    /**
     * Instance unique pré-initialisée
     */
    private static Affluence INSTANCE = new Affluence();

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static Affluence getInstance() {
        return INSTANCE;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void setRequestQueue(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }
}
