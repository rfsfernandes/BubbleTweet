package pt.rfernandes.bubbletweet.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import pt.rfernandes.bubbletweet.model.CustomUser;

@Database(entities = {CustomUser.class},
    version = DBContract.DATABASE_VERSION,
    exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase INSTANCE;

  public abstract DatabaseDAO getTemplateDAO();

  public static AppDatabase getInstance(final Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = buildDatabase(context);
        }
      }
    }
    return INSTANCE;
  }

  private static AppDatabase buildDatabase(final Context context) {
    return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
        DBContract.DATABASE_NAME)
        .addCallback(new RoomDatabase.Callback() {
          @Override
          public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

          }
        })
        .build();
  }

}
