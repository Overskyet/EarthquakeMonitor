package overskyet.earthquakemonitor;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CreditsAlertDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.credits_layout, null));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView creditsBody = alertDialog.findViewById(R.id.credits_dialog_body);
        creditsBody.setMovementMethod(LinkMovementMethod.getInstance());

        return alertDialog;
    }
}
