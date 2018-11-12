package com.ettud.is74task4;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ettud.is74task4.Models.TermViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@TargetApi(Build.VERSION_CODES.N)
public class RecyclerViewTermAdapter extends RecyclerView.Adapter<RecyclerViewTermAdapter.ViewHolder> {
    public Optional<List<TermViewModel>> termViewModels;

    public RecyclerViewTermAdapter(List<TermViewModel> termViewModels) {
        this.termViewModels = Optional.ofNullable(termViewModels);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_term, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if(termViewModels.isPresent()) {
            TermViewModel termViewModel = termViewModels.get().get(i);
            viewHolder.setTermViewModel(termViewModel);
        }
    }

    @Override
    public int getItemCount() {
        return termViewModels.isPresent() ? termViewModels.get().size() : 0;
    }

    public void setTermSynonims(List<TermViewModel> termViewModels) {
        if(termViewModels == null)
            this.termViewModels = Optional.of(new ArrayList<>());
        else
            this.termViewModels = Optional.of(termViewModels);
        notifyDataSetChanged();
    }

    public void clear() {
        this.termViewModels = Optional.of(new ArrayList<>());
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private LinearLayout mDefinitionsLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDefinitionsLayout = (LinearLayout) itemView.findViewById(R.id.definitionsLayout);
        }

        public void setTermViewModel(TermViewModel termViewModel){
            setTitle(termViewModel);
            mDefinitionsLayout.removeAllViews();
            for (TermViewModel.TermSourceModel termSourceModel : termViewModel.sources) {
                addSource(termSourceModel);
            }
        }

        private void setTitle(TermViewModel termViewModel){
            String toHtml = "<b>" + termViewModel.term + "</b> ";
            for (String synonym: termViewModel.synonims) {
                toHtml += "<i>" + synonym + "</i>";
            }
            mTitle.setText(Html.fromHtml(toHtml));
        }

        private void addSource(TermViewModel.TermSourceModel termSourceModel){
            TextView textView = new TextView(mDefinitionsLayout.getContext());
            String toHtml = "";
            for (TermViewModel.TermSourceModel.DefinitionModel definitionModel: termSourceModel.definitions) {
                toHtml += "<pre>&#9;</pre>" + definitionModel.definition;
            }
            toHtml += "\n" + " (<i>" + termSourceModel.sourceName + "</i>)";
            textView.setText(Html.fromHtml(toHtml));
            mDefinitionsLayout.addView(textView);
        }
    }
}
