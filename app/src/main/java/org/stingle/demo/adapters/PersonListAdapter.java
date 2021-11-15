package org.stingle.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.stingle.demo.R;
import org.stingle.demo.data.PersonAndImages;

public class PersonListAdapter extends ListAdapter<PersonAndImages, PersonListAdapter.ViewHolder> {

	private Context context;
	private LayoutInflater layoutInflater;

	private int selectedFaceIndex;

	private FaceSelectListener faceSelectListener;

	public PersonListAdapter(Context context) {
		super(new PersonDiffCallback());

		this.context = context;

		layoutInflater = LayoutInflater.from(context);
	}

	public void setFaceSelectListener(FaceSelectListener faceSelectListener) {
		this.faceSelectListener = faceSelectListener;
	}

	public PersonAndImages getSelectedFace() {
		if (selectedFaceIndex >= 0 && selectedFaceIndex < getItemCount()) {
			return getItem(selectedFaceIndex);
		} else {
			return null;
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(layoutInflater.inflate(R.layout.item_face, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.imageView.setImageBitmap(getItem(position).getThumbnail(context));
		holder.selectorView.setActivated(selectedFaceIndex == position);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = holder.getAdapterPosition();
				if (position != RecyclerView.NO_POSITION) {
					updateSelection(position);
					if (faceSelectListener != null) {
						faceSelectListener.onPersonSelected(getItem(position));
					}
				}
			}
		});
	}

	private void updateSelection(int newSelection) {
		int oldSelection = selectedFaceIndex;
		selectedFaceIndex = newSelection;

		notifyItemChanged(oldSelection);
		notifyItemChanged(newSelection);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		SimpleDraweeView imageView;
		ImageView selectorView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
			selectorView = itemView.findViewById(R.id.selector);
		}
	}

	static class PersonDiffCallback extends DiffUtil.ItemCallback<PersonAndImages> {

		@Override
		public boolean areItemsTheSame(@NonNull PersonAndImages oldItem, @NonNull PersonAndImages newItem) {
			return oldItem.person.id.equals(newItem.person.id);
		}

		@Override
		public boolean areContentsTheSame(@NonNull PersonAndImages oldItem, @NonNull PersonAndImages newItem) {
			return areItemsTheSame(oldItem, newItem);
		}
	}

	public interface FaceSelectListener {
		void onPersonSelected(PersonAndImages person);
	}
}
