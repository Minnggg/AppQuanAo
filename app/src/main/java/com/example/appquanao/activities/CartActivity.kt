package com.example.appquanao.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquanao.Model.GioHangModel
import com.example.appquanao.R
import com.example.appquanao.adapter.CartAdapter
import com.example.appquanao.databinding.ActivityCartBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class CartActivity : AppCompatActivity() {
    private var ListGioHang = mutableListOf<GioHangModel>()
    private lateinit var binding : ActivityCartBinding
    var check = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_cart)
        setContentView(binding.root)

        initView(applicationContext)
        val database = Firebase.database
        getListGioHang(applicationContext,database)
    }

    private fun initView(applicationContext: Context) {
         val database = Firebase.database

        binding.BtnBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
        //xử lý sự kiện đặt hàng
        binding.btnDatHang.setOnClickListener(View.OnClickListener {
            if(check)
            {
                var sharedPref : SharedPreferences = getApplicationContext().getSharedPreferences("AppQuanAo", Context.MODE_PRIVATE)
                //thêm vào lịch sử đặt hàng
                database.reference.child("lichsudathang").child(sharedPref.getString("idNguoiDung","").toString()).child("sodon").get().addOnSuccessListener {
                    if(it.value!= null){
                        database.getReference("lichsudathang/"+sharedPref.getString("idNguoiDung","").toString()).child((it.value.toString().toInt()+1).toString()).setValue(ListGioHang)
                        database.getReference("lichsudathang/"+sharedPref.getString("idNguoiDung","").toString()).child("sodon").setValue(it.value.toString().toInt()+1)
                    }
                    else
                    {
                        database.getReference("lichsudathang/"+sharedPref.getString("idNguoiDung","").toString()).child(("1").toString()).setValue(ListGioHang)
                        database.getReference("lichsudathang/"+sharedPref.getString("idNguoiDung","").toString()).child("sodon").setValue(1)
                    }
                    //xóa khỏi danh sách giỏ hàng
                    database.getReference("giohang/"+sharedPref.getString("idNguoiDung","").toString()).setValue(null)

                }.addOnFailureListener{
                }
                showDialog(it.context,database,sharedPref)
            }


        })

    }
    //hiển thị thông báo
    private fun showDialog(
        contextt: Context,
        database: FirebaseDatabase,
        sharedPref: SharedPreferences,
    ) {
        val builder = AlertDialog.Builder(contextt)
        builder.setTitle("Thông báo")
        builder.setMessage("Đặt hàng thành công")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    //lấy và hiển thị danh sách giỏ hàng
    private fun getListGioHang(context: Context, database: FirebaseDatabase) {
        var sharedPref : SharedPreferences = context.getSharedPreferences("AppQuanAo", Context.MODE_PRIVATE)


        //lấy data từ firebase
        val giohang =  database.getReference("giohang/"+sharedPref.getString("idNguoiDung","").toString())
        giohang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sum = 0
                ListGioHang = mutableListOf<GioHangModel>()
                for (snapshot in dataSnapshot.children) {
                    val yourObject = snapshot.getValue(GioHangModel::class.java)
                    yourObject?.let {
                        ListGioHang.add(it)
                        sum = sum + it.price.toString().replace(".", "").toInt()* it.soluong!!
                    }
                }

                if(ListGioHang.size!=0) check = true
                else check = false
                //cài adapter cho recyclerview
                val adapter : CartAdapter = CartAdapter(ListGioHang,context)
                var layoutManager = LinearLayoutManager(context)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                binding.rvListProduct.layoutManager = layoutManager
                binding.rvListProduct.adapter = adapter

                binding.tvTongTien.text = String.format("%,d",sum)+" đ"

            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}

