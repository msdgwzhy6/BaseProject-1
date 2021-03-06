package com.vincent.baseproject.ui.login

import android.app.Dialog
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.haoge.easyandroid.easy.EasyToast
import com.vincent.baselibrary.helper.EditTextInputHelper
import com.vincent.baseproject.R
import com.vincent.baseproject.common.UIActivity
import com.vincent.baseproject.data.LoginRepository
import com.vincent.dialoglibrary.MessageDialog
import com.vincent.dialoglibrary.ToastDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.app_toolbar.*
import java.util.regex.Pattern

class LoginActivity : UIActivity(), LifecycleOwner {

    private lateinit var viewModel: LoginViewModel
    private val mEditTextInputHelper by lazy {
        EditTextInputHelper(login_btn_commit)
    }
    override fun getLayoutId(): Int {
       return R.layout.activity_login
    }


    override fun initView() {
        tv_rightMenu.text = "注册"
        tv_rightMenu.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))
        tv_rightMenu.visibility = View.VISIBLE
        initToolBar(app_toolbar,false)
        mEditTextInputHelper.addView(login_et_password,login_et_email)
    }

    override fun initData() {
        super.initData()
        viewModel = ViewModelProviders.of(this,object :ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LoginViewModel(LoginRepository.getInstance()) as T
            }
        })[LoginViewModel::class.java]
    }
    override fun initEvent() {
        super.initEvent()
        tv_rightMenu.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        login_tv_forget.setOnClickListener {
            MessageDialog.Builder(this)
                .setTitle("温馨提示") // 标题可以不用填写
                .setMessage("暂不支持找回密码，记不起来了可以重新注册一个")
                .setConfirm("注册")
                .setCancel("我在想想") // 设置 null 表示不显示取消按钮
                //.setAutoDismiss(false) // 设置点击按钮后不关闭对话框
                .apply {
                    this.mListener = object : com.vincent.dialoglibrary.MessageDialog.OnListener {
                        override fun confirm(dialog: Dialog) {
                            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                        }

                        override fun cancel(dialog: Dialog) {

                        }


                    }
                }

                .show()
        }
        login_btn_commit.setOnClickListener {
            val username = login_et_email.text.toString()
            val password = login_et_password.text.toString()
            if (!checkUserName(username)){
                EasyToast.DEFAULT.show("请输入正确的邮箱地址")
                return@setOnClickListener
            }
            if(!checkPassword(password)){
                EasyToast.DEFAULT.show("密码不符合规范\n以字母开头，长度在6~18之间，只能包含字母、数字和下划线")
                return@setOnClickListener
            }
            viewModel.logun(username, password)
        }

        viewModel.loginState.observe(this, Observer {
            when(it.status){
                0 -> {
                    ToastDialog.Builder(this)
                        .setType(ToastDialog.Type.FINISH)
                        .setMessage(it.data!!)
                        .show()
                }
                1 -> {
                    ToastDialog.Builder(this)
                        .setType(ToastDialog.Type.ERROR)
                        .setMessage(it.message!!)
                        .show()
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        mEditTextInputHelper.removeViews()
    }

    /**
     * 不建议登录页开启侧滑
     */
    override fun isSupportSwipeBack(): Boolean {
        return false
    }

    private fun checkUserName(username:String):Boolean{
        if(username.isBlank())return false
        val regExp = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+com"
        val p = Pattern.compile(regExp)
        val m = p.matcher(username)
        return m.matches()
    }

    private fun checkPassword(password:String):Boolean{
        if(password.isBlank())return false
        val regExp = "^[a-zA-Z]\\w{5,17}\$"
        val p = Pattern.compile(regExp)
        val m = p.matcher(password)
        return m.matches()
    }

}
