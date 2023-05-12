package com.example.healthcare.adapter

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.databinding.PostViewItemBinding
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.PostForDoctor
import com.example.healthcare.screen.doctor.AddPostFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DoctorPostView(var context: Context, var array: ArrayList<PostForDoctor>) :
    RecyclerView.Adapter<DoctorPostView.ViewHolder>() {
    private var firestore = Firebase.firestore
    private var count = 0
    private var isShow: Boolean? = true
    private var map = mutableMapOf<String, Boolean>()

    class ViewHolder(var binding: PostViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = PostViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getShowOrNot(array[position].idPost, holder.binding.hideButton)
        holder.binding.textViewTitle.text = array[position].title
        holder.binding.textViewBody.text = array[position].text
        holder.binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("هل انت متاكد من حذف المقالة ؟")
                .setMessage("في حال قمت بالضغط على نعم سوف يتم حذف المقالة بشكل نهائي")
                .setPositiveButton("نعم") { _, _ ->
                    deleteData(position, array[position].idPost)
                }
                .setNegativeButton("لا") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        holder.binding.eyeButton.setOnClickListener {
            Log.d("aaa", "this is id post : ${array[position].idPost}")
            count++
            if (count % 2 == 0) {
                holder.binding.eyeButton.setBackgroundResource(R.drawable.hide)
                changeHigh(holder.binding.linerLayoutEye)
            } else {
                holder.binding.eyeButton.setBackgroundResource(R.drawable.icon_eye)
                changeHigh(holder.binding.linerLayoutEye)
                firestore.collection("viewPost").document(array[position].idPost)
                    .collection("PostIs")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        holder.binding.linerLayoutEye.removeAllViewsInLayout()
                        for (i in snapshot.documents) {
                            val userID = i.getString("userID")
                            firestore.collection("users").document(userID!!).get()
                                .addOnSuccessListener {
                                    val name = it.getString("name")
                                    val myTextView = TextView(context)
                                    myTextView.text = name
                                    myTextView.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.black
                                        )
                                    )
                                    myTextView.textSize = 15f
                                    val myTypeface = ResourcesCompat.getFont(context, R.font.font)
                                    myTextView.typeface = myTypeface
                                    val layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(16, 16, 50, 16)
                                    myTextView.layoutParams = layoutParams
                                    holder.binding.linerLayoutEye.addView(myTextView)
                                }
                        }
                    }
            }
        }

        holder.binding.editButton.setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val fragment = AddPostFragment.newInstance(array[position].title, array[position].text)
            fragmentManager.beginTransaction()
                .replace(R.id.continer, fragment)
                .commit()
        }
        holder.binding.hideButton.setOnClickListener {
            val s = map[array[position].idPost]
            if (s!!) {
                hidePost(array[position].idPost)
                getShowOrNot(array[position].idPost, holder.binding.hideButton)
            } else {
                appearPost(array[position].idPost)
                getShowOrNot(array[position].idPost, holder.binding.hideButton)
            }
        }
    }

    private fun deleteData(position: Int, postId: String) {
        firebase().getNameSick {
            val ref = firestore.collection("posts-$it")
            ref.document(postId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show()
                    array.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, array.size)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "تم حدوث خطا يرجى المحاولة لاحقا",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }

    }

    private fun changeHigh(linerLayout: LinearLayout) {
        var newHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        val anim: ValueAnimator
        if (count % 2 == 0) {
            newHeight = 0
            anim = ValueAnimator.ofInt(linerLayout.height, newHeight)
        } else {
            anim = ValueAnimator.ofInt(linerLayout.height, newHeight)
        }
        anim.duration = 500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.addUpdateListener { valueAnimator ->
            val layoutParams = linerLayout.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            linerLayout.layoutParams = layoutParams
        }
        anim.start()
    }

    private fun hidePost(postId: String) {
        firebase().getNameSick {
            firestore.collection("posts-$it").document(postId)
                .update("isShow", false)
                .addOnSuccessListener {
                    Toast.makeText(context, "تم اخفاء المقالة", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "لقد حدث خطا قم بالمحاولة في وقت لاحق",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


    }

    private fun appearPost(postId: String) {
        firebase().getNameSick {
            firestore.collection("posts-$it").document(postId)
                .update("isShow", true)
                .addOnSuccessListener {
                    Toast.makeText(context, "تم اظهار المقالة", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "لقد حدث خطا قم بالمحاولة في وقت لاحق",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun getShowOrNot(postId: String, button: Button) {
        firebase().getNameSick {
            firestore.collection("posts-$it").document(postId)
                .get()
                .addOnSuccessListener {
                    isShow = it.getBoolean("isShow")
                    map[postId] = isShow!!
                    if (isShow == true) {
                        button.text = "اخفاء"
                    } else {
                        button.text = "اظهار"
                    }
                }
        }

    }
}