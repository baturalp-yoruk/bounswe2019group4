package com.example.arken.util;

import com.example.arken.model.Alert;
import com.example.arken.model.Annotation;
import com.example.arken.model.Article;
import com.example.arken.model.ArticleCreateRequest;
import com.example.arken.model.ArticleRateRequest;
import com.example.arken.model.Comment;
import com.example.arken.model.Email;
import com.example.arken.model.EventWithComment;
import com.example.arken.model.GetPortfolio;
import com.example.arken.model.GoogleId;
import com.example.arken.model.GoogleUser;
import com.example.arken.model.ListAlert;
import com.example.arken.model.ListArticle;
import com.example.arken.model.ListEvent;
import com.example.arken.model.ListNotification;
import com.example.arken.model.LoginUser;
import com.example.arken.model.Portfolio;
import com.example.arken.model.Profile;
import com.example.arken.model.Recommendation;
import com.example.arken.model.SearchResult;
import com.example.arken.model.SignupUser;
import com.example.arken.model.User;
import com.example.arken.model.investment.Account;
import com.example.arken.model.investment.Deposit;
import com.example.arken.model.investment.Investment;
import com.example.arken.model.investment.ListOrder;
import com.example.arken.model.investment.Order;
import com.example.arken.model.tradingEquipment.Currency;
import com.example.arken.model.tradingEquipment.ListCurrency;
import com.example.arken.model.tradingEquipment.Prediction;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    @Headers({"Content-Type: application/json"})
    @POST("auth/signup")
    Call<ResponseBody> signup(
            @Body SignupUser signupUser
    );

    @Headers({"Content-Type: application/json"})
    @POST("auth/login")
    Call<User> login(
            @Body LoginUser loginUser
    );

    @Headers({"Content-Type: application/json"})
    @POST("auth/forget-password")
    Call<ResponseBody> forgetPassword(
            @Body Email email
    );

    @Headers({"Content-Type: application/json"})
    @POST("auth/google")
    Call<User> google(
            @Body GoogleId googleId
    );

    @Headers({"Content-Type: application/json"})
    @POST("auth/signup")
    Call<User> signupGoogle(
            @Body GoogleUser googleUser
    );

    @Headers({"Content-Type: application/json"})
    @POST("comments")
    Call<ResponseBody> makeComment(@Header("Cookie") String userCookie,
                                   @Body Comment comment
    );

    @Headers({"Content-Type: application/json"})
    @GET("events")
    Call<ListEvent> getEvents(@Query("country") String country, @Query("importance") Integer importance, @Query("page") Integer s, @Query("limit") Integer k);


    @Headers({"Content-Type: application/json"})
    @GET("trading-equipments")
    Call<ListCurrency> getCurrentCurrencyValues();

    @Headers({"Content-Type: application/json"})
    @GET("trading-equipments/{id}")
    Call<Currency> getCurrency(@Header("Cookie") String cookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @POST("trading-equipments/follow")
    Call<Currency> followCurrency(@Header("Cookie") String cookie, @Query("tEq") String k);

    @Headers({"Content-Type: application/json"})
    @POST("trading-equipments/unfollow")
    Call<Currency> unFollowCurrency(@Header("Cookie") String cookie, @Query("tEq") String k);

    @Headers({"Content-Type: application/json"})
    @GET("events/{id}")
    Call<EventWithComment> getEvent(@Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @GET("profile/{id}")
    Call<Profile> getProfile(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @DELETE("comments/event/{id}")
    Call<ResponseBody> deleteEventComment(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @DELETE("comments/article/{id}")
    Call<ResponseBody> deleteArticleComment(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @DELETE("comments/trading-equipment/{id}")
    Call<ResponseBody> deleteTEComment(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @GET("search")
    Call<SearchResult> search(@Query("q") String query);

    @Headers({"Content-Type: application/json"})
    @GET("profile/{id}/follow")
    Call<ResponseBody> follow(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @GET("profile/{id}/unfollow")
    Call<ResponseBody> unfollow(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @GET("profile/accept/{id}")
    Call<ResponseBody> accept(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @GET("profile/reject/{id}")
    Call<ResponseBody> reject(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @POST("trading-equipments/prediction")
    Call<Currency> predictionCurrency(@Header("Cookie") String cookie, @Body Prediction prediction);

    @Headers({"Content-Type: application/json"})
    @POST("articles")
    Call<ResponseBody> createArticle(
            @Header("Cookie") String userCookie, @Body Article article
    );

    @Headers({"Content-Type: application/json"})
    @GET("articles/{id}")
    Call<Article> getArticle(
            @Header("Cookie") String userCookie, @Path("id") String article
    );

    @Headers({"Content-Type: application/json"})
    @GET("profile/cancel/{id}")
    Call<ResponseBody> cancelReq(@Header("Cookie") String userCookie, @Path("id") String k);

    @Headers({"Content-Type: application/json"})
    @DELETE("articles/{id}")
    Call<ResponseBody> deleteArticle(
            @Header("Cookie") String userCookie, @Path("id") String articleId
    );

    @Headers({"Content-Type: application/json"})
    @PATCH("articles/{id}")
    Call<ResponseBody> editArticle(
            @Header("Cookie") String userCookie, @Path("id") String articleId, @Body ArticleCreateRequest articleCreateRequest
    );

    @Headers({"Content-Type: application/json"})
    @GET("articles")
    Call<ListArticle> getArticles(
            @Query("page") Integer s, @Query("limit") Integer k);

    @Headers({"Content-Type: application/json"})
    @POST("articles/{id}/rate")
    Call<ResponseBody> rateArticle( @Header("Cookie") String userCookie,@Path("id") String id, @Body ArticleRateRequest articleRateRequest);

    @Headers({"Content-Type: application/json"})
    @DELETE("portfolios/{id}")
    Call<ResponseBody> deletePortfolio( @Header("Cookie") String userCookie,@Path("id") String id);

    @Headers({"Content-Type: application/json"})
    @POST("portfolios")
    Call<ResponseBody> createPortfolio(@Header("Cookie") String cookie, @Body Portfolio portfolio);

    @Headers({"Content-Type: application/json"})
    @PATCH("portfolios/{id}")
    Call<ResponseBody> editPortfolio(@Header("Cookie") String cookie, @Path("id") String id, @Body Portfolio portfolio);

    @Headers({"Content-Type: application/json"})
    @GET("portfolios/{id}")
    Call<GetPortfolio> getPortfolio(@Header("Cookie") String userCookie, @Path("id") String id );

    @Headers({"Content-Type: application/json"})
    @POST("portfolios/{id}/follow")
    Call<ResponseBody> followPortfolio(@Header("Cookie") String userCookie, @Path("id") String id );

    @Headers({"Content-Type: application/json"})
    @POST("portfolios/{id}/unfollow")
    Call<ResponseBody> unfollowPortfolio(@Header("Cookie") String userCookie, @Path("id") String id );

    @Headers({"Content-Type: application/json"})
    @GET("trading-equipments/alert")
    Call<ListAlert> getAlerts(@Header("Cookie") String userCookie);

    @Headers({"Content-Type: application/json"})
    @POST("trading-equipments/alert")
    Call<ResponseBody> createAlert(@Header("Cookie") String userCookie, @Body Alert alert);

    @Headers({"Content-Type: application/json"})
    @DELETE("trading-equipments/alert/{id}")
    Call<ResponseBody> deleteeAlert(@Header("Cookie") String userCookie, @Path("id") String id);

    @Headers({"Content-Type: application/json"})
    @GET("notifications")
    Call<ListNotification> getNotifications(@Header("Cookie") String userCookie);

    @Headers({"Content-Type: application/json"})
    @GET("recommendations")
    Call<Recommendation> getRecommendations(@Header("Cookie") String userCookie);
    @Headers({"Content-Type: application/json"})
    @GET("investments")
    Call<Investment> getInvestment(@Header("Cookie") String userCookie);

    @Headers({"Content-Type: application/json"})
    @GET("investments/order")
    Call<ListOrder> getOrder(@Header("Cookie") String userCookie);

    @Headers({"Content-Type: application/json"})
    @POST("investments/deposit")
    Call<Account> depositMoney(@Header("Cookie") String userCookie, @Body Deposit deposit);

    @Headers({"Content-Type: application/json"})
    @POST("investments/order")
    Call<ResponseBody> sendOrder(@Header("Cookie") String userCookie, @Body Order order);

    @Headers({"Content-Type: application/json"})
    @POST("investments/sell")
    Call<Account> sellTeq(@Header("Cookie") String userCookie, @Body Deposit sell);

    @Headers({"Content-Type: application/json"})
    @POST("investments/buy")
    Call<Account> buyTeq(@Header("Cookie") String userCookie, @Body Deposit buy);

    @Headers({"Content-Type: application/json"})
    @DELETE("investments/order/{id}")
    Call<ResponseBody> deleteOrder(@Header("Cookie") String userCookie, @Path("id") String orderId);
}
