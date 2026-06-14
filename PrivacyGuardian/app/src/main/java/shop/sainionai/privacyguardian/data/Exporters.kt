package shop.sainionai.privacyguardian.data

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import shop.sainionai.privacyguardian.model.ScannedApp
import java.io.File

/** CSV and PDF report exporters (Phase: reporting). All output stays on device. */
object Exporters {

    fun csv(apps: List<ScannedApp>): String {
        val sb = StringBuilder()
        sb.append("package,label,version,risk,level,confidence,trackers,granted_sensitive\n")
        for (a in apps) {
            fun esc(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
            sb.append(esc(a.packageName)).append(',')
                .append(esc(a.appLabel)).append(',')
                .append(esc(a.versionName ?: "")).append(',')
                .append(a.risk.overall).append(',')
                .append(a.risk.level.name).append(',')
                .append("%.2f".format(a.risk.confidence)).append(',')
                .append(a.trackers.size).append(',')
                .append(a.grantedSensitive.size).append('\n')
        }
        return sb.toString()
    }

    /** Renders a simple paginated PDF and returns the written file. */
    fun pdf(apps: List<ScannedApp>, outFile: File): File {
        val doc = PdfDocument()
        val title = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val head = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val body = Paint().apply { textSize = 11f }

        val pageW = 595; val pageH = 842; val margin = 40f
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
        var canvas = page.canvas
        var y = margin

        canvas.drawText("Privacy Guardian — scan report", margin, y, title); y += 28f
        canvas.drawText("Apps: ${apps.size}", margin, y, body); y += 22f
        canvas.drawText("App", margin, y, head)
        canvas.drawText("Risk", margin + 320, y, head)
        canvas.drawText("Trackers", margin + 400, y, head); y += 16f

        for (a in apps) {
            if (y > pageH - margin) {
                doc.finishPage(page); pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
                canvas = page.canvas; y = margin
            }
            val name = a.appLabel.take(46)
            canvas.drawText(name, margin, y, body)
            canvas.drawText("${a.risk.overall} ${a.risk.level.name}", margin + 320, y, body)
            canvas.drawText("${a.trackers.size}", margin + 400, y, body)
            y += 16f
        }
        doc.finishPage(page)
        outFile.outputStream().use { doc.writeTo(it) }
        doc.close()
        return outFile
    }
}
