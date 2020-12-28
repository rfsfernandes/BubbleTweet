# How to get remote data

To make an HTTPS request configure the DataSource class with your base url.

After that, configure the TemplateService with the endpoint you desire. Use **GET** or **POST** (etc)
annotations to make **GET** or **POST** requests.
For example:

    @POST("/api/v1/login")
   	Call<ResponseObject> login(@Body User user, @Header("Language") String language);    


That ResponseObject must be a model that matches the json response.

Since you have configured all of the above, use the DataSource object to get the TemplateService.
After that, just call the method you need.