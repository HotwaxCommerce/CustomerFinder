import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityFind
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue

import java.sql.Timestamp
import java.time.ZonedDateTime

import static java.math.BigDecimal.*

ExecutionContext ec = context.ec
EntityFind ef = ec.entity.find("customerFinder.FindCustomerView").distinct(true)
ef.selectField("partyId")
if (partyId) {
    ef.condition(
            ec.entity.conditionFactory.makeCondition("partyId", EntityCondition.LIKE, partyId + "%").ignoreCase()
    )
}

if (emailAddress) {
    MailList = [
            ec.entity.conditionFactory.makeCondition("infoString", EntityCondition.EQUALS, emailAddress).ignoreCase(),
            ec.entity.conditionFactory.makeCondition("contactMechPurposeId", EntityCondition.EQUALS, "GeneralEmail").ignoreCase()]
    ef.condition(
            ec.entity.conditionFactory.makeCondition(MailList, EntityCondition.AND)
    );
}

if (combinedName) {
    String first_name = combinedName
    String last_name = combinedName
    if (combinedName.contains(" ")) {
        first_name = combinedName.substring(0, combinedName.indexOf(" "))
        last_name = combinedName.substring(combinedName.indexOf(" ") + 1)
    }
    OrderedList = [
            ec.entity.conditionFactory.makeCondition("firstName", EntityCondition.LIKE, first_name + "%").ignoreCase(),
            ec.entity.conditionFactory.makeCondition("lastName", EntityCondition.LIKE,  last_name + "%").ignoreCase()]
    ef.condition(
            ec.entity.conditionFactory.makeCondition(OrderedList, EntityCondition.OR)
    );

    ef.orderBy(combinedName);
}

if (contactNumber) {
    ef.condition(
            ec.entity.conditionFactory.makeCondition("contactNumber", EntityCondition.EQUALS, contactNumber ).ignoreCase()
    )
}


if (address1) {
    ef.condition(
            ec.entity.conditionFactory.makeCondition("address1", EntityCondition.EQUALS, address).ignoreCase()
    )
}

if (city) {
    ef.condition(
            ec.entity.conditionFactory.makeCondition("city", EntityCondition.EQUALS, city ).ignoreCase()
    )
}


if (postalCode) {
    ef.condition(
            ec.entity.conditionFactory.makeCondition("postalCode", EntityCondition.EQUALS, postalCode ).ignoreCase()
    )
}

if (!pageNoLimit) {
    ef.offset(pageIndex as int, pageSize as int);
    ef.limit(pageSize as int)
}

partyIdList = []
EntityList el = ef.list()
//el.filterByDate("fromDate", "thruDate", nowTimestamp)
for (EntityValue ev in (el as List<EntityValue>)) partyIdList.add(ev.partyId)

count = ef.count()
listPageIndex = ef.pageIndex
listPageSize = ef.pageSize
low = listPageIndex * listPageSize + 1
high = (listPageIndex * listPageSize) + listPageSize
if (high > count) high = count