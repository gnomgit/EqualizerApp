package com.example.myapplication.Controller;

import com.android.volley.Request;
import com.android.volley.Response;
import com.example.myapplication.Model.Constants;

public class PersonServices {

    public void userById (android.content.Context context, long pId, final VolleyCallback callback, final Response.ErrorListener errorListener) {

        String URL = Constants.URL_UserById + "?pId=" + pId;

        GenericServices.callService(context, Request.Method.GET, URL, null,null, callback, errorListener);

    }

    public void userByEmail (android.content.Context context, String user, VolleyCallback callback, final Response.ErrorListener errorListener) {

        String URL = Constants.URL_CheckUser + "?u=" + user;

        GenericServices.callService(context, Request.Method.GET, URL, null, null, callback, errorListener);
    }

    public void userByEmailAndPass (android.content.Context context, String user, String password, final VolleyCallback callback, final Response.ErrorListener errorListener) {

        String URL = Constants.URL_CheckUserAndPass + "?uName=" + user + "&uPass=" + password;

        //GenericServices.TOKEN = "273425c6-fdee-4f22-8f50-5abe86af2313";
        GenericServices.callService(context, Request.Method.GET, URL, null, null, callback, errorListener);
    }

    public void usersList(android.content.Context context, final VolleyCallback callback, final Response.ErrorListener errorListener) {

        String URL = Constants.URL_UsersList;

        GenericServices.callService(context, Request.Method.GET, URL, null, null, callback, errorListener);
    }

    public void contacts(android.content.Context context, long pId, final VolleyCallback callback, final Response.ErrorListener errorListener) {

        String URL = Constants.URL_Contacts + "?pId=" + pId;

        GenericServices.callListService(context, Request.Method.GET, URL, null, null, callback, errorListener);
    }
}
