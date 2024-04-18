import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.attendance.attendance_module_list_object
import com.miniproject.attendx.databinding.AttendanceModuleListItemBinding

interface AttendanceModuleClickListener {
    fun onAttendanceModuleClicked(attendanceModule: attendance_module_list_object)
}

class Module_list_RecyclerView_adapter(
    private val data: ArrayList<attendance_module_list_object>,
    private val listener: AttendanceModuleClickListener
) : RecyclerView.Adapter<Module_list_RecyclerView_adapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AttendanceModuleListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(private val binding: AttendanceModuleListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(attendanceModuleListObject: attendance_module_list_object) {
            binding.attendanceModListAttName.text = attendanceModuleListObject.attName
            binding.attendanceListModuleItemContainer.setOnClickListener {
                // Pass the clicked attendance module to the listener
                listener.onAttendanceModuleClicked(attendanceModuleListObject)
            }
        }
    }
}
