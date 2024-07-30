package com.xck.str.xml;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTMLParser {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\xuchengkun\\Desktop\\心理抚养.html";
        Document document = Jsoup.parse(new File(path), "UTF-8");

        String classChapterTitle = "chapter-title";
        String classReadContent = "read-content";

        List<Element> elements = new ArrayList<>();
        Map<String, Element> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            Element element = document.getElementById("chapter" + i);
            if (element != null) {
                map.put("chapter" + i,  element);
            }
        }

        path = "D:\\心理抚养1.html";
        document = Jsoup.parse(new File(path), "UTF-8");
        for (int i = 0; i < 100; i++) {
            Element element = document.getElementById("chapter" + i);
            if (element != null) {
                map.put("chapter" + i,  element);
            }
        }

        path = "D:\\心理抚养3.html";
        document = Jsoup.parse(new File(path), "UTF-8");
        for (int i = 0; i < 100; i++) {
            Element element = document.getElementById("chapter" + i);
            if (element != null) {
                map.put("chapter" + i,  element);
            }
        }

        for (int i = 0; i < 100; i++) {
            Element element = map.get("chapter" + i);
            if (element != null) {
                elements.add(element);
            }
        }

        com.itextpdf.text.Document itextDocument = new com.itextpdf.text.Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(itextDocument, new FileOutputStream("D:\\心理抚养4.pdf"));
        itextDocument.open();

        BaseFont baseFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font bigTitleFont = new Font(baseFont, 32, Font.BOLD);
        Font sectionFont = new Font(baseFont, 27, Font.BOLD);
        Font borderFont = new Font(baseFont, 24, Font.NORMAL);
        Font normalFont = new Font(baseFont, 19, Font.NORMAL);

        for (Element element : elements) {
            String chapterText = element.getElementsByClass(classChapterTitle).text();
            Paragraph chapterTitle = new Paragraph(chapterText, bigTitleFont);
            chapterTitle.setSpacingAfter(20);
            Chapter chapter = new Chapter(chapterTitle, 1);
            chapter.setNumberDepth(0);
            itextDocument.add(chapter);

            boolean isFirst = false;
            Section secondChapter = null;
            for (Element e : element.getElementsByClass(classReadContent)) {
                Element p = e.child(0);

                String content = p.text();
                if (!content.endsWith("。") && !content.endsWith("，")
                        && !content.endsWith("？") && !content.endsWith("”")
                        && !content.endsWith("…") && !content.endsWith("）")
                        && !content.endsWith("：") && !content.endsWith("！")
                        && !content.endsWith("；") && !content.equals("李玫瑾") && !content.equals("2021年立春")) {
                    String littleTitle = content;

                    Paragraph sectionTitle = new Paragraph(littleTitle, sectionFont);
                    sectionTitle.setSpacingAfter(15);
                    sectionTitle.setSpacingBefore(15);
                    secondChapter = chapter.addSection(sectionTitle);
                    itextDocument.add(secondChapter);
                    isFirst = false;
                } else {
                    // 章节的前面一段，框起来

                    if (secondChapter != null && !chapterText.equals("序言") && !isFirst) {
                        isFirst = true;

                        PdfPTable table = new PdfPTable(1);
                        table.setWidthPercentage(100);
                        PdfPCell cell = new PdfPCell();
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setCellEvent(new BoxBorder(PdfPCell.LEFT | PdfPCell.RIGHT, BaseColor.RED));
                        cell.addElement(new Paragraph(content, borderFont));
                        table.addCell(cell);

                        table.setSpacingBefore(20);
                        table.setSpacingAfter(20);

                        itextDocument.add(table);
                        continue;
                    }

                    Paragraph contentP = new Paragraph(content, normalFont);
                    contentP.setSpacingAfter(5);
                    contentP.setSpacingBefore(5);
                    if (secondChapter != null) {
                        itextDocument.add(contentP);
                    } else {
                        itextDocument.add(contentP);
                    }
                }
            }
        }

        pdfWriter.flush();
        itextDocument.close();
        pdfWriter.close();
    }

    static class BoxBorder implements PdfPCellEvent {
        private int border;
        private BaseColor color;

        public BoxBorder(int border, BaseColor color) {
            this.border = border;
            this.color = color;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.saveState();
            canvas.setColorStroke(color);
            canvas.setLineWidth(4f);
            if ((border & PdfPCell.LEFT) == PdfPCell.LEFT) {
                canvas.moveTo(position.getLeft(), position.getTop());
                canvas.lineTo(position.getLeft(), position.getBottom());
            }
            if ((border & PdfPCell.RIGHT) == PdfPCell.RIGHT) {
                canvas.moveTo(position.getRight(), position.getTop());
                canvas.lineTo(position.getRight(), position.getBottom());
            }
            canvas.stroke();
            canvas.restoreState();
        }
    }
}
