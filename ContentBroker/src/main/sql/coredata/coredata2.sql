begin;
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(1,'image/jp2','-m JPEG2000-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(2,'image/jpeg','-m JPEG-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(3,'application/pdf','-m PDF-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(4,'image/tiff','-m TIFF-hul');

insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(5,'image/gif','');

insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(6,'text/html','-m UTF8-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(7,'application/xhtml+xml','-m UTF8-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(8,'text/html','-m UTF8-hul');

insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(9,'application/xml, text/xml','-m UTF8-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(10,'application/xml','-m UTF8-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(11,'text/xml','-m UTF8-hul');

/*//following lines are commented because jhove throw strange errors for valid xml,html files
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(5,'image/gif','-m GIF-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(6,'text/html','-m HTML-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(7,'application/xhtml+xml','-m HTML-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(8,'text/html','-m HTML-hul'); 
  
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(9,'application/xml, text/xml','-m XML-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(10,'application/xml','-m XML-hul');
insert into jhove_parameter_mapping(id, mime_type, map_parameter) values(11,'text/xml','-m XML-hul');
*/

commit;