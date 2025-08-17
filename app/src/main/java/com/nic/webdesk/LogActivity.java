package com.nic.webdesk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class LogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WebdeskDAO dao;

    //--------------------------------------------------------------- onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //---------------------------------------- activity_log.xml
        setContentView(R.layout.activity_log);

        recyclerView = findViewById(R.id.recyclerViewLog);
        dao = new WebdeskDAO(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        Map<String, Object> result = dao.readAllLog();

        if (result != null && result.containsKey("logs")) {
            List<Map<String, String>> logList = (List<Map<String, String>>) result.get("logs");
            recyclerView.setAdapter(new LogAdapter(logList));
        } else {
            Log.d("LOG_ACTIVITY", "Nessun log trovato");
        }
    }

    //--------------------------------------------------------------- Inner Adapter Class
    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
        private final List<Map<String, String>> logs;
        public LogAdapter(List<Map<String, String>> logs) {
            this.logs = logs;
        }
        @NonNull
        @Override
        //---------------------------------------- activity_log_item,xml
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_log_item, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            Map<String, String> log = logs.get(position);
            holder.idLog.setText(log.get("id"));
            holder.dateLog.setText(log.get("date"));
            holder.typeLog.setText(log.get("type"));
            holder.statusLog.setText(log.get("status"));
            holder.noteLog.setText(log.get("note"));
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class LogViewHolder extends RecyclerView.ViewHolder {
            TextView idLog, dateLog, typeLog, statusLog, noteLog;

            public LogViewHolder(@NonNull View itemView) {
                super(itemView);
                idLog = itemView.findViewById(R.id.idLog);
                dateLog = itemView.findViewById(R.id.dateLog);
                typeLog = itemView.findViewById(R.id.typeLog);
                statusLog = itemView.findViewById(R.id.statusLog);
                noteLog = itemView.findViewById(R.id.noteLog);
            }
        }
    }

    //------------------------------------------------------------------ delete all log
    // Delete all log and return to home page
    public void onDeleteButton(View view) {
        String message= "Vuoi cancellare il file dei Log?\nL'operazione è irreversibile.\n\nProcedo con la cancellazione del file dei Log?";
        Alert.alertYesNoDialog(this, "File dei Log",message, new Alert.YesNoCallback() {
            @Override
            public void onResult(boolean yes) {
                if (yes) {
                    dao.deleteAllLog();
                    onHomeButton(view);
                }
            }
        });
    }
    //================================================================== goto main_View
    // Torna alla pagina principale
    public void onHomeButton(View view) {
        Intent intent = new Intent(LogActivity.this, ToolActivity.class);
        startActivity(intent);
        finish(); // Opzionale: chiude l'attività corrente dopo aver avviato la ActivityMain
    }
}

