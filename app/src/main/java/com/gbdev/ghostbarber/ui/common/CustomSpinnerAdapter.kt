import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.ui.hairstyle.Hairstyle

class CustomSpinnerAdapter(context: Context, private val hairstyles: List<Hairstyle>) :
    ArrayAdapter<Hairstyle>(context, 0, hairstyles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_item)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup, resource: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val hairstyle = getItem(position)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val textView = view.findViewById<TextView>(R.id.textView)

        hairstyle?.let {
            imageView.setImageResource(it.imageResId)
            textView.text = it.name
        }

        return view
    }
}
