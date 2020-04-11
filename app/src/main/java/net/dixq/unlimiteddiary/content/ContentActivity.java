package net.dixq.unlimiteddiary.content;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import net.dixq.unlimiteddiary.R;

import static net.dixq.unlimiteddiary.content.IntentTagKt.TAG_NEW;

public class ContentActivity extends AppCompatActivity {

    public final static int RESULT_CREATED = 2;
    public final static int RESULT_EDITED  = 3;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.main_comtent);
        // Fragmentを作成します
        Fragment fragment;
        if(getIntent().getStringExtra(TAG_NEW)!=null){
            fragment = new PostFragment();
        } else {
            fragment = new DetailFragment();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.layout_root, fragment);
        transaction.commit();
    }

    public void changeFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.layout_root, fragment);
        transaction.commit();
    }

}
