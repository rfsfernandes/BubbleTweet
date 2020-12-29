package pt.rfernandes.bubbletweet.custom.template_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import pt.rfernandes.bubbletweet.R;
import androidx.recyclerview.widget.RecyclerView;


public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {
	 private final Context mContext;
	 private List<Object> objectList;

	 /**
		* Instatiate a new TemplateAdapter.
		*
		* @param context This is the acticity context. If you instatiate this object in a fragment, use getContext. If you instatiate this object in an activity, use this.
		*/
	 public TemplateAdapter(Context context) {
			this.mContext = context;
			this.objectList = new ArrayList<>();
	 }


	 @NonNull
	 @Override
	 public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.template_row, viewGroup, false);
			return new TemplateViewHolder(view);
	 }

	 @Override
	 public void onBindViewHolder(@NonNull TemplateViewHolder viewHolder, int i) {
			viewHolder.bindView(objectList.get(i));
	 }

	@Override
	 public int getItemCount() {
		return objectList.size();
	 }

	 class TemplateViewHolder extends RecyclerView.ViewHolder {

			/**
			 * Instatiates a new ViewHolder and binds the view with ButterKnife.
			 *
			 * @param itemView The layout of each row.
			 */

			TemplateViewHolder(@NonNull View itemView) {
				 super(itemView);
//				 ButterKnife.bind(this, itemView);
			}

			/**
			 * Binds the views using the object of the list.
			 *
			 * @param object One object of the list, to populate a row.
			 */
			void bindView(Object object) {

			}

	 }

	 /**
		* Refreshes the objectList and notifies the adapter that the dataset has changed.
		*
		* @param objectList The updated list to send to this adapter.
		*/

	 public void refreshList(List<Object> objectList) {
			this.objectList = objectList;
			notifyDataSetChanged();
	 }

}