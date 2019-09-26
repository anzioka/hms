import xlsxwriter

with open ("icd10cm_codes_2017.txt") as f:
	book = xlsxwriter.Workbook("icd10.xlsx")
	sheet = book.add_worksheet("ICD-10 Disease Codes");

	bold = book.add_format({'bold' : True})
	sheet.write(0, 0, "Code", bold)
	sheet.write(0, 1, "Name", bold)

	row = 1;
	for line in f:
		code  = line[0:7]
		name = line[8:]
		sheet.write(row, 0, code.strip())
		sheet.write(row, 1, name.strip())
		row += 1

	book.close()
