package pt.rfernandes.bubbletweet.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DataSource {

	 private static final String BASE_URL = "https://api.twitter.com/1.1/";

	 private static final Retrofit retrofit = new Retrofit.Builder()
					 .baseUrl(BASE_URL)
					 .addConverterFactory(GsonConverterFactory.create())
					 .build();

	 private static TemplateService templateService;

	 public static TemplateService getTemplateService() {
			if (templateService == null) {
				 templateService = retrofit.create(TemplateService.class);
			}
			return templateService;
	 }

}