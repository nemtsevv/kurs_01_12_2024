package com.example.kurs_01_12_2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenseList;

    // Конструктор адаптера
    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Отображаем сумму с валютой (BYN)
        holder.amountTextView.setText(String.format(Locale.getDefault(), "%.2f BYN", expense.getAmount()));

        // Преобразуем дату для отображения
        holder.dateTextView.setText(formatDateForDisplay(expense.getDate()));

        // Используем 'type' как описание расхода
        holder.descriptionTextView.setText(expense.getType());
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Обновляем список расходов с сортировкой
    public void updateExpenses(List<Expense> newExpenses) {
        this.expenseList = newExpenses;
        sortExpenses();  // Сортируем список после обновления
        notifyDataSetChanged();
    }

    // Сортировка списка расходов по датам (d/M/yyyy)
    private void sortExpenses() {
        Collections.sort(expenseList, (e1, e2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date date1 = sdf.parse(e1.getDate());
                Date date2 = sdf.parse(e2.getDate());
                return date1.compareTo(date2);  // Сравниваем даты
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;  // Если ошибка, оставляем как есть
            }
        });
    }

    // ViewHolder для расходов
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView;
        TextView dateTextView;
        TextView descriptionTextView;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.text_amount);
            dateTextView = itemView.findViewById(R.id.text_date);
            descriptionTextView = itemView.findViewById(R.id.text_description);
        }
    }

    // Метод для форматирования даты из формата yyyy-MM-dd в d/M/yyyy для отображения
    private String formatDateForDisplay(String date) {
        try {
            // Ожидаемый формат даты, как в базе данных (yyyy-MM-dd)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // Формат для отображения, например 1/12/2024
            SimpleDateFormat outputFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

            Date parsedDate = inputFormat.parse(date);
            if (parsedDate != null) {
                return outputFormat.format(parsedDate); // Возвращаем дату в формате d/M/yyyy
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;  // Возвращаем исходную дату, если не удалось преобразовать
    }
}
