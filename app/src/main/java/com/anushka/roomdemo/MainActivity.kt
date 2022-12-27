package com.anushka.roomdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.anushka.roomdemo.databinding.ActivityMainBinding
import com.anushka.roomdemo.db.Subscriber
import com.anushka.roomdemo.db.SubscriberDatabase
import com.anushka.roomdemo.db.SubscriberRepository

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SubscriberViewModel
    private lateinit var adapter: MyRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val dao = SubscriberDatabase.getInstance(applicationContext).subscriberDAO
        val repository = SubscriberRepository(dao)
        val factory = SubscriberViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SubscriberViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initRecyclerView()

        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initRecyclerView() {
        binding.subscriberRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter =
            MyRecyclerViewAdapter { selectedItem: Subscriber ->
                listItemClicked(selectedItem)
            }
        binding.subscriberRecyclerview.adapter = adapter
        displaySubscribersList()
    }

    private fun displaySubscribersList() {
        viewModel.getSavedSubscribers().observe(this, Observer {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun listItemClicked(subscriber: Subscriber) {
        //   Toast.makeText(this, subscriber.name, Toast.LENGTH_SHORT).show()
        viewModel.initUpdateAndDelete(subscriber)
    }
}