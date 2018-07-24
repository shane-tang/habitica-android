package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.SparseArray
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

class SkillTasksActivity : BaseActivity() {

    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val viewPager: ViewPager by bindView(R.id.viewPager)
    private val tabLayout: TabLayout by bindView(R.id.tab_layout)

    internal var viewFragmentsDictionary = SparseArray<SkillTasksRecyclerViewFragment>()

    override fun getLayoutResId(): Int {
        return R.layout.activity_skill_tasks
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTaskLists()
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    private fun loadTaskLists() {
        val fragmentManager = supportFragmentManager

        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                val fragment = SkillTasksRecyclerViewFragment()
                when (position) {
                    0 -> fragment.taskType = Task.TYPE_HABIT
                    1 -> fragment.taskType = Task.TYPE_DAILY
                    else -> fragment.taskType = Task.TYPE_TODO
                }

                compositeSubscription.add(fragment.taskSelectionEvents.subscribe(Consumer { task -> taskSelected(task) }, RxErrorHandler.handleEmptyError()))

                viewFragmentsDictionary.put(position, fragment)

                return fragment
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence? {
                when (position) {
                    0 -> return getString(R.string.habits)
                    1 -> return getString(R.string.dailies)
                    2 -> return getString(R.string.todos)
                }
                return ""
            }
        }


        tabLayout.setupWithViewPager(viewPager)
    }

    fun taskSelected(task: Task) {
        val resultIntent = Intent()
        resultIntent.putExtra("task_id", task.id)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
