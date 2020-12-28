package pt.rfernandes.bubbletweet.data.local;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import pt.rfernandes.bubbletweet.model.CustomUser;

@Dao
public interface DatabaseDAO {

  /**
   * Use:
   *
   * @Query("SELECT * FROM Object")
   *     List<Object> getAllObjects();
   *
   * To get all objects from the table Object
   */

  @Query("DELETE FROM CustomUser WHERE id = :id")
  void deleteUser(long id);

  @Insert
  void insertUser(CustomUser customUser);

  @Query("SELECT * FROM CustomUser")
  List<CustomUser> getUser();

}
