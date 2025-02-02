package com.example.todolist

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.db.AppDatabase
import com.example.todolist.db.TodoDao
import com.example.todolist.db.TodoEntity
import java.util.Objects

class MainActivity : ComponentActivity(), OnItemLongClickListener {

    private lateinit var binding : ActivityMainBinding

    private lateinit var db : AppDatabase
    private lateinit var todoDao : TodoDao
    private lateinit var todoList : ArrayList<TodoEntity>
    private lateinit var adapter : TodoRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!!
        todoDao = db.getTodoDao()

        getAllTodoList()

        binding.btnAddTodo.setOnClickListener {
            Log.d("MainActivity", "Button clicked!")
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAllTodoList() {
        Thread{
            todoList = ArrayList(todoDao.getAllTodo())
            setRecyclerView()
        }.start()
    }

    private fun setRecyclerView() {
        //ui는 항상 메인 스레드에서 접근해야 함
        runOnUiThread {
            adapter = TodoRecyclerViewAdapter(todoList, this)
            binding.recyclearview.adapter = adapter
            binding.recyclearview.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onRestart() {
        super.onRestart()
        getAllTodoList()
    }

    override fun onLongClick(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.alert_title))
        builder.setMessage(getString(R.string.alert_message))
        builder.setNegativeButton(getString(R.string.alert_no), null)
        builder.setPositiveButton(getString(R.string.alert_yes),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    deleteTodo(position)
                }
            }
        )
        builder.show()
    }

    private fun deleteTodo(position: Int) {
        Thread{
            todoDao.deleteTodo(todoList[position])
            todoList.removeAt(position)
            runOnUiThread {
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}

