package com.tencent.uikit.app.main

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.TUIThemeManager
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import com.tencent.uikit.app.R
import com.trtc.tuikit.common.imageloader.ImageLoader

class MainFragment : Fragment() {
    private var userCenter: ImageFilterView? = null
    private var recyclerMain: RecyclerView? = null
    private var trtcMainAdapter: TRTCMainAdapter? = null
    private val notification: ITUINotification = object : ITUINotification {
        override fun onNotifyEvent(key: String?, subKey: String?, param: MutableMap<String?, Any?>) {
            Log.i(TAG, "key=$key,subKey=$subKey,param=$param")
            if (TextUtils.equals(param[TUIConstants.TUILogin.SELF_ID] as String?, TUILogin.getLoginUser())) {
                ImageLoader.load(activity, userCenter, TUILogin.getFaceUrl(), R.drawable.app_ic_avatar)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.app_fragment_main, container, false)
        initView(rootView)
        registerEvent()
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unRegisterEvent()
    }

    private fun initView(rootView: View) {
        val mainTitle = rootView.findViewById<ImageView?>(R.id.img_main_icon)
        recyclerMain = rootView.findViewById<RecyclerView?>(R.id.rv_main_list)
        mainTitle?.setBackgroundResource(R.drawable.app_title_zh)
        if (TextUtils.equals(TUIThemeManager.getInstance().currentLanguage, "en")) {
            mainTitle?.setBackgroundResource(R.drawable.app_title_en)
        }
        userCenter = rootView.findViewById<ImageFilterView?>(R.id.img_user_center)
        ImageLoader.load(activity, userCenter, TUILogin.getFaceUrl(), R.drawable.app_ic_avatar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        userCenter?.setOnClickListener { v: View? -> navController.navigate(R.id.mine_fragment) }
        val trtcMainData = TRTCMainData()
        trtcMainAdapter = TRTCMainAdapter(
            isSmallScreenDevice, TUIThemeManager.getInstance().currentLanguage,
            trtcMainData.itemDataList as MutableList<MainItemData>, object : TRTCMainAdapter.OnItemClickListener {
                override fun onItemClick(mainItemData: MainItemData?) {
                    mainItemData?.let { item ->
                        val type: Int = item.itemType?.type ?: 0
                        val intent = Intent(context, item.itemTargetClass)
                        intent.putExtra("TITLE", getString(item.itemTitle))
                        intent.putExtra("TYPE", type)
                        startActivity(intent)
                    }
                }
            })
        val gridLayoutManager = GridLayoutManager(getContext(), 2)
        recyclerMain?.setLayoutManager(gridLayoutManager)
        recyclerMain?.setAdapter(trtcMainAdapter)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImageLoader.load(getActivity(), userCenter, TUILogin.getFaceUrl(), R.drawable.app_ic_avatar)
    }

    private val isSmallScreenDevice: Boolean
        get() {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels <= 720
        }

    private fun registerEvent() {
        TUICore.registerEvent(
            TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_INFO_UPDATED, notification
        )
    }

    private fun unRegisterEvent() {
        TUICore.unRegisterEvent(
            TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_INFO_UPDATED, notification
        )
    }

    companion object {
        private const val TAG = "MainFragment"
        const val url = "https://cloud.tencent.com/act/event/report-platform"

    }

    private data class SimpleItem(
        val iconRes: Int,
        val title: String,
        val subtitle: String
    )

    private class SimpleAdapter(
        private val items: List<SimpleItem>,
        private val onItemClick: () -> Unit
    ) : RecyclerView.Adapter<SimpleAdapter.SimpleVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleVH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.app_main_item, parent, false)
            return SimpleVH(view)
        }

        override fun onBindViewHolder(holder: SimpleVH, position: Int) {
            val item = items[position]
            holder.icon.setImageResource(item.iconRes)
            holder.title.text = item.title
            holder.subtitle.text = item.subtitle
            holder.tag.text = ""
            holder.itemView.setOnClickListener { onItemClick() }
        }

        override fun getItemCount(): Int = items.size

        class SimpleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.img_main_icon)
            val title: TextView = itemView.findViewById(R.id.tv_main_title)
            val tag: TextView = itemView.findViewById(R.id.tv_main_tag)
            val subtitle: TextView = itemView.findViewById(R.id.tv_main_subtitle)
        }
    }
}